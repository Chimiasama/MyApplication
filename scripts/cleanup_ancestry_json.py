import json

def process_file(filepath):
    print(f"Cleaning {filepath}...")
    with open(filepath, 'r', encoding='utf-8') as f:
        data = json.load(f)

    for entry in data:
        seen_ids = {} # Map ID -> index in new_list
        new_list = []

        for hab in entry.get("habilidades", []):
            hid = hab["id"]
            cat = hab.get("category", "")

            if hid in seen_ids:
                existing_idx = seen_ids[hid]
                existing = new_list[existing_idx]
                existing_cat = existing.get("category", "")

                # Logic: Prefer racial_hindrance/edge over trait
                priority = {"racial_hindrance": 3, "racial_edge": 3, "racial_trait_positive": 2, "racial_trait_negative": 2}

                curr_p = priority.get(cat, 1)
                exist_p = priority.get(existing_cat, 1)

                if curr_p > exist_p:
                    # Replace existing with current
                    new_list[existing_idx] = hab
                elif curr_p == exist_p:
                    # If same priority, maybe check description length or something?
                    # Usually the one from 'desvantagens' list (later) might be just the name, while 'habilidades' has desc.
                    # If existing has description and current is just name, keep existing.
                    if len(existing.get("descricao", "")) > len(hab.get("descricao", "")):
                        pass # Keep existing
                    else:
                        new_list[existing_idx] = hab
            else:
                new_list.append(hab)
                seen_ids[hid] = len(new_list) - 1

        entry["habilidades"] = new_list

    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

process_file("app/src/main/assets/basico_ancestralidades.json")
process_file("app/src/main/assets/fantasia_ancestralidades.json")
process_file("app/src/main/assets/scifi_ancestralidades.json")
