import requests
from faker import Faker
import random
import time

API_BASE_URL = "http://localhost:8080"
CREATE_ACCOUNT_ENDPOINT = f"{API_BASE_URL}/accounts"

fake = Faker("pt_BR")

HEADERS = {
    "Content-Type": "application/json"
}

TOTAL_ACCOUNTS = 100
INITIAL_BALANCE = "1000.00"
CURRENCY = "BRL"


def generate_valid_name(min_len=3, max_len=20) -> str:
    while True:
        name = fake.name()
        name = " ".join(name.split())  # remove espaços duplicados

        if len(name) < min_len:
            continue

        if len(name) > max_len:
            name = name[:max_len].rstrip()

        if min_len <= len(name) <= max_len:
            return name


def create_account(index: int):
    payload = {
        "name": generate_valid_name(),
        "currency": CURRENCY,
        "initialBalance": INITIAL_BALANCE
    }

    response = requests.post(
        CREATE_ACCOUNT_ENDPOINT,
        json=payload,
        headers=HEADERS,
        timeout=5
    )

    if response.status_code in (200, 201):
        data = response.json()
        print(f"[{index:03}] ✅ Account created | id={data.get('accountId')}")
        return data.get("accountId")
    else:
        print(
            f"[{index:03}] ❌ Failed | "
            f"status={response.status_code} | body={response.text}"
        )
        return None


def main():
    print("🚀 Creating accounts...\n")

    account_ids = []

    for i in range(1, TOTAL_ACCOUNTS + 1):
        account_id = create_account(i)
        if account_id:
            account_ids.append(account_id)

        # pequeno delay para não esmagar o backend
        time.sleep(random.uniform(0.02, 0.1))

    print("\n🎉 Done!")
    print(f"✅ Created {len(account_ids)} accounts")

    # salva os IDs para testes futuros
    with open("account_ids.txt", "w") as f:
        for acc_id in account_ids:
            f.write(f"\"{acc_id}\",\n")

    print("📁 account_ids.txt generated")


if __name__ == "__main__":
    main()