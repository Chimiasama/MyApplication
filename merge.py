import json
import sys
import os

def merge(target_path, source_path):
    print(f"Merging {source_path} into {target_path}...")
    try:
        with open(target_path, 'r') as f:
            existing = json.load(f)

        with open(source_path, 'r') as f:
            new_items = json.load(f)

        print(f"Existing count: {len(existing)}")
        print(f"New items count: {len(new_items)}")

        # Check for duplicates by ID to avoid growing the file indefinitely on retries
        existing_ids = {item.get('id') for item in existing if 'id' in item}
        # Note: Equipamentos has nested structure (list of categories -> itens), so we handle it differently

        is_equipment = "equipamentos.json" in target_path

        if is_equipment:
            # Simple append for equipment categories for now, or check structure
            # The temp file has a list of categories. We should append these categories to the main list.
            # existing is a list of categories.
            existing.extend(new_items)
            print("Appended equipment categories.")
        else:
            # Standard list of objects with IDs
            for item in new_items:
                if item.get('id') not in existing_ids:
                    existing.append(item)
                else:
                    print(f"Skipping duplicate: {item.get('id')}")

        with open(target_path, 'w') as f:
            json.dump(existing, f, indent=2, ensure_ascii=False)

        print(f"Success. New count: {len(existing)}")

    except Exception as e:
        print(f"Error merging {target_path}: {e}")
        sys.exit(1)

if __name__ == "__main__":
    merge("app/src/main/assets/poderes.json", "temp_new_powers.json")
    merge("app/src/main/assets/complicacoes.json", "temp_complicacoes.json")
    merge("app/src/main/assets/Vantagens.json", "temp_vantagens.json")
    # Equipamentos worked previously but let's re-run carefully or skip if checked?
    # The user said LeMat was there. But if I run it again without check, I might duplicate.
    # I'll check if LeMat is already there in the python script logic if I could, but equipment structure is different.
    # I'll skip equipment since I verified it.
    # merge("app/src/main/assets/equipamentos.json", "temp_equipamentos.json")
