import json

with open("app/src/main/assets/poderes.json", "r") as f:
    data = json.load(f)

origins = set()
for item in data:
    origins.add(item.get("origem", "UNKNOWN"))

print("Origins found:", origins)
