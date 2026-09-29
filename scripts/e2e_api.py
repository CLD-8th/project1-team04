#!/usr/bin/env python3
"""Run a bounded API edge-case matrix against the Compose app.

Creates three test users and 21 small products. Existing records are untouched.
"""

import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid


BASE_URL = os.environ.get("API_BASE_URL", "http://127.0.0.1:8080").rstrip("/")
PASSWORD = "E2ePassword123!"
PNG = __import__("base64").b64decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/D8sAAAAASUVORK5CYII="
)
RUN_ID = uuid.uuid4().hex[:10]
checks = []


def multipart(fields, image=PNG, content_type="image/png"):
    boundary = "----usedtrade-" + uuid.uuid4().hex
    parts = []
    for name, value in fields.items():
        parts.append((
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"{name}\"\r\n\r\n{value}\r\n"
        ).encode())
    if image is not None:
        parts.append((
            f"--{boundary}\r\nContent-Disposition: form-data; name=\"image\"; "
            f"filename=\"test.png\"\r\nContent-Type: {content_type}\r\n\r\n"
        ).encode() + image + b"\r\n")
    parts.append(f"--{boundary}--\r\n".encode())
    return b"".join(parts), f"multipart/form-data; boundary={boundary}"


def call(method, path, token=None, payload=None, form=None):
    headers = {}
    body = None
    if token:
        headers["Authorization"] = "Bearer " + token
    if payload is not None:
        headers["Content-Type"] = "application/json"
        body = json.dumps(payload).encode()
    if form is not None:
        body, headers["Content-Type"] = form
    request = urllib.request.Request(BASE_URL + path, body, headers, method=method)
    try:
        response = urllib.request.urlopen(request, timeout=15)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        raw = response.read()
        if "json" in response.headers.get("Content-Type", "") and raw:
            result = json.loads(raw)
        else:
            result = raw if raw else None
        return response.status, result


def check(fr, name, method, path, expected, *, token=None, payload=None, form=None, verify=None):
    status, result = call(method, path, token, payload, form)
    if status != expected:
        message = result.get("message") if isinstance(result, dict) else str(result)[:100]
        raise AssertionError(f"{fr} {name}: expected {expected}, got {status}: {message}")
    if verify is not None and not verify(result):
        raise AssertionError(f"{fr} {name}: response assertion failed")
    checks.append(fr)
    print(f"PASS {fr} {name}: HTTP {status}")
    return result


def product_form(title, *, price="12345", category=None, image=PNG, content_type="image/png"):
    fields = {
        "title": title,
        "content": "Bounded API edge-case test",
        "category": category or "E2E-" + RUN_ID,
        "price": price,
    }
    return multipart(fields, image, content_type)


def main():
    health = check("INFRA", "health", "GET", "/actuator/health", 200,
                   verify=lambda value: value["status"] == "UP")
    users = {}
    for role in ("seller", "buyer", "other"):
        email = f"{role}-{RUN_ID}@example.test"
        users[role] = {"email": email}
        result = check("FR-10", f"{role} signup", "POST", "/api/users", 201,
                       payload={"email": email, "password": PASSWORD, "nickname": role})
        users[role]["id"] = result["id"]

    seller = users["seller"]
    buyer = users["buyer"]
    other = users["other"]
    check("FR-10", "duplicate email", "POST", "/api/users", 409,
          payload={"email": seller["email"], "password": PASSWORD, "nickname": "again"})
    check("FR-10", "invalid email", "POST", "/api/users", 400,
          payload={"email": "bad-email", "password": PASSWORD, "nickname": "bad"})
    check("FR-10", "short password", "POST", "/api/users", 400,
          payload={"email": f"short-{RUN_ID}@example.test", "password": "123", "nickname": "bad"})
    check("FR-10", "blank nickname", "POST", "/api/users", 400,
          payload={"email": f"blank-{RUN_ID}@example.test", "password": PASSWORD, "nickname": ""})

    for role, user in users.items():
        result = check("FR-08", f"{role} login", "POST", "/api/users/login", 200,
                       payload={"email": user["email"], "password": PASSWORD},
                       verify=lambda value: bool(value.get("accessToken")))
        user["token"] = result["accessToken"]
        user["refresh"] = result["refreshToken"]
    check("FR-08", "wrong password", "POST", "/api/users/login", 401,
          payload={"email": seller["email"], "password": "wrong-password"})
    check("FR-08", "unknown email", "POST", "/api/users/login", 401,
          payload={"email": f"unknown-{RUN_ID}@example.test", "password": PASSWORD})
    check("FR-08", "missing password", "POST", "/api/users/login", 400,
          payload={"email": seller["email"]})

    check("FR-01", "anonymous registration", "POST", "/api/products", 401,
          form=product_form("anonymous"))
    first = check("FR-01", "PNG registration", "POST", "/api/products", 201,
                  token=seller["token"], form=product_form("E2E first " + RUN_ID),
                  verify=lambda value: value["status"] == "SELLING" and value["sellerId"] == seller["id"])
    second = check("FR-01", "second registration", "POST", "/api/products", 201,
                   token=seller["token"], form=product_form("E2E second " + RUN_ID))
    first_id, second_id = first["id"], second["id"]
    image_status, image_body = call("GET", first["imagePath"])
    if image_status != 200 or image_body != PNG:
        raise AssertionError("FR-01 uploaded image did not round-trip")
    print("PASS FR-01 image download: HTTP 200 and identical bytes")
    check("FR-01", "missing image", "POST", "/api/products", 400,
          token=seller["token"], form=product_form("no-image", image=None))
    check("FR-01", "wrong media type", "POST", "/api/products", 400,
          token=seller["token"], form=product_form("text-image", content_type="text/plain"))
    check("FR-01", "negative price", "POST", "/api/products", 400,
          token=seller["token"], form=product_form("negative", price="-1"))
    check("FR-01", "overlong title", "POST", "/api/products", 400,
          token=seller["token"], form=product_form("T" * 201))
    check("FR-01", "integer overflow price", "POST", "/api/products", 400,
          token=seller["token"], form=product_form("overflow", price="2147483648"))

    check("FR-02", "public list", "GET", "/api/products", 200,
          verify=lambda value: any(row["id"] == first_id for row in value))
    category = urllib.parse.quote("E2E-" + RUN_ID)
    check("FR-02", "category filter", "GET", "/api/products?category=" + category, 200,
          verify=lambda value: len(value) >= 2 and all(row["category"] == "E2E-" + RUN_ID for row in value))
    check("FR-02", "status filter", "GET", "/api/products?status=SELLING", 200,
          verify=lambda value: any(row["id"] == first_id for row in value))
    check("FR-02", "invalid status", "GET", "/api/products?status=INVALID", 400)

    check("FR-07", "anonymous recent list", "GET", "/api/products/recent", 401)
    check("FR-07", "empty buyer history", "GET", "/api/products/recent", 200,
          token=buyer["token"], verify=lambda value: value == [])
    check("FR-05", "public detail", "GET", f"/api/products/{first_id}", 200,
          verify=lambda value: value["id"] == first_id)
    check("FR-07", "public view leaves history empty", "GET", "/api/products/recent", 200,
          token=buyer["token"], verify=lambda value: value == [])
    check("FR-05", "unknown product", "GET", "/api/products/99999999", 404)
    check("FR-05", "malformed product ID", "GET", "/api/products/not-an-id", 400)
    for product_id in (first_id, second_id, first_id):
        check("FR-05", "authenticated detail", "GET", f"/api/products/{product_id}", 200,
              token=buyer["token"])
    check("FR-07", "recent order and deduplication", "GET", "/api/products/recent", 200,
          token=buyer["token"], verify=lambda value: [row["id"] for row in value] == [first_id, second_id])
    check("FR-07", "user isolation", "GET", "/api/products/recent", 200,
          token=seller["token"], verify=lambda value: value == [])
    check("FR-07", "refresh token is not access token", "GET", "/api/products/recent", 401,
          token=buyer["refresh"])

    check("FR-06", "empty seller deal list", "GET", f"/api/products/{first_id}/deals", 200,
          token=seller["token"], verify=lambda value: value == [])
    check("FR-06", "anonymous deal list", "GET", f"/api/products/{first_id}/deals", 401)
    check("FR-06", "buyer cannot read deal list", "GET", f"/api/products/{first_id}/deals", 403,
          token=buyer["token"])
    check("FR-06", "unknown product", "GET", "/api/products/99999999/deals", 404,
          token=seller["token"])
    check("FR-03", "anonymous application", "POST", f"/api/products/{first_id}/application", 401)
    check("FR-03", "seller self-application", "POST", f"/api/products/{first_id}/application", 403,
          token=seller["token"])
    check("FR-03", "unknown product", "POST", "/api/products/99999999/application", 404,
          token=buyer["token"])
    deal = check("FR-03", "buyer application", "POST", f"/api/products/{first_id}/application", 201,
                 token=buyer["token"], verify=lambda value: value["buyerId"] == buyer["id"])
    deal_id = deal["dealId"]
    check("FR-03", "duplicate application", "POST", f"/api/products/{first_id}/application", 409,
          token=buyer["token"])
    other_deal = check("FR-03", "second buyer application", "POST", f"/api/products/{first_id}/application", 201,
                       token=other["token"])
    check("FR-06", "seller sees both applications", "GET", f"/api/products/{first_id}/deals", 200,
          token=seller["token"], verify=lambda value: {row["dealId"] for row in value} == {deal_id, other_deal["dealId"]})
    check("FR-04", "anonymous approval", "POST", f"/api/deals/{deal_id}/approve", 401)
    check("FR-04", "buyer cannot approve", "POST", f"/api/deals/{deal_id}/approve", 403,
          token=buyer["token"])
    check("FR-04", "unknown deal", "POST", "/api/deals/99999999/approve", 404,
          token=seller["token"])
    check("FR-04", "seller approval", "POST", f"/api/deals/{deal_id}/approve", 200,
          token=seller["token"], verify=lambda value: value["status"] == "APPROVED" and value["productStatus"] == "SOLD")
    check("FR-04", "repeat approval", "POST", f"/api/deals/{deal_id}/approve", 409,
          token=seller["token"])
    check("FR-04", "other pending deal cannot approve", "POST", f"/api/deals/{other_deal['dealId']}/approve", 409,
          token=seller["token"])
    check("FR-03", "sold product rejects application", "POST", f"/api/products/{first_id}/application", 409,
          token=other["token"])
    check("FR-02", "sold status filter", "GET", "/api/products?status=SOLD&category=" + category, 200,
          verify=lambda value: any(row["id"] == first_id and row["status"] == "SOLD" for row in value))
    check("FR-07", "sold product remains in recent", "GET", "/api/products/recent", 200,
          token=buyer["token"], verify=lambda value: value[0]["id"] == first_id and value[0]["status"] == "SOLD")

    extra_ids = []
    for index in range(19):
        result = check("FR-01", f"recent capacity seed {index + 1}/19", "POST", "/api/products", 201,
                       token=seller["token"], form=product_form(f"E2E seed {RUN_ID}-{index}"))
        extra_ids.append(result["id"])
        check("FR-05", f"recent capacity view {index + 1}/19", "GET", f"/api/products/{result['id']}", 200,
              token=buyer["token"])
    check("FR-07", "20-item cap and newest-first order", "GET", "/api/products/recent", 200,
          token=buyer["token"], verify=lambda value: len(value) == 20 and value[0]["id"] == extra_ids[-1]
          and value[-1]["id"] == first_id and second_id not in {row["id"] for row in value})

    check("FR-09", "anonymous logout", "POST", "/api/users/logout", 401)
    check("FR-09", "seller logout", "POST", "/api/users/logout", 204, token=seller["token"])
    check("FR-09", "replayed access token rejected", "GET", "/api/products/recent", 401,
          token=seller["token"])
    check("FR-09", "other account stays logged in", "GET", "/api/products/recent", 200,
          token=buyer["token"])
    fresh = check("FR-08", "immediate login after logout", "POST", "/api/users/login", 200,
                  payload={"email": seller["email"], "password": PASSWORD})
    if fresh["accessToken"] == seller["token"]:
        raise AssertionError("A new login reissued the revoked token")
    check("FR-09", "new access token works", "GET", "/api/products/recent", 200,
          token=fresh["accessToken"])
    check("FR-09", "buyer logout", "POST", "/api/users/logout", 204, token=buyer["token"])
    check("FR-09", "buyer token replay rejected", "GET", "/api/products/recent", 401,
          token=buyer["token"])

    print(f"PASS all {len(checks)} HTTP cases across 10 APIs; run={RUN_ID}, "
          f"seller={seller['id']}, buyer={buyer['id']}, product={first_id}, deal={deal_id}")
    print(f"REDIS_RECENT_KEY=recent:products:{buyer['id']}")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"FAIL {error}", file=sys.stderr)
        sys.exit(1)
