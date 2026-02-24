import json

def process_file(filepath):
    print(f"Fixing ADAPTAVEL category in {filepath}...")
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for entry in data:
        for hab in entry.get("habilidades", []):
            if hab.get("id") == "ADAPTAVEL":
                # User request: "Adaptável não é um racial_edge mas sim uma racial_trait_positive"
                hab["category"] = "racial_trait_positive"

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

process_file("app/src/main/assets/basico_ancestralidades.json")
process_file("app/src/main/assets/fantasia_ancestralidades.json")
process_file("app/src/main/assets/scifi_ancestralidades.json")
