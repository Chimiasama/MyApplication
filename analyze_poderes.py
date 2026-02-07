import json
from pathlib import Path

assets_dir = Path("app/src/main/assets")
poderes_files = sorted(assets_dir.glob("*_poderes.json"))

origins = set()
for file_path in poderes_files:
    with file_path.open("r", encoding="utf-8") as f:
        data = json.load(f)
    for item in data:
        origins.add(item.get("origem", "UNKNOWN"))

print("Origins found:", sorted(origins))
