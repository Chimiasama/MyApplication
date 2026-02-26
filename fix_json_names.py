import json
import os
import re

def to_fancy_title_case(text):
    if not text:
        return text

    # Normalize: replace underscores with spaces and trim
    normalized = text.replace('_', ' ').strip()

    # Split by whitespace
    words = re.split(r'\s+', normalized)

    lower_case_words = {
        "de", "da", "do", "das", "dos",
        "e", "em", "no", "na", "nos", "nas",
        "por", "para", "com", "sem", "sob", "sobre",
        "a", "o", "as", "os", "à", "às", "ou"
    }

    upper_case_words = {
        "XP", "PA", "PB", "PP", "PV", "PC", "PE", "SP", "GM", "MJ", "CD", "ME", "VE", "NV"
    }

    roman_numerals = {
        "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X",
        "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX", "XX"
    }

    special_prefixes = ["d'", "l'"]

    def is_word_char(c):
        return c.isalnum() or c == "'" or c in "áéíóúâêîôûãõàèìòùäëïöüçñÁÉÍÓÚÂÊÎÔÛÃÕÀÈÌÒÙÄËÏÖÜÇÑ"

    processed_words = []

    for index, raw_token in enumerate(words):
        # Separate punctuation wrapper
        prefix = ""
        suffix = ""

        # Take while not word char (punctuation at start)
        i = 0
        while i < len(raw_token) and not is_word_char(raw_token[i]):
            prefix += raw_token[i]
            i += 1

        # Take while not word char (punctuation at end)
        j = len(raw_token) - 1
        while j >= i and not is_word_char(raw_token[j]):
            suffix = raw_token[j] + suffix # Prepend to suffix to keep order
            j -= 1

        core = raw_token[i : j + 1]

        if not core:
            processed_words.append(raw_token)
            continue

        def process_segment(segment, is_first_word_of_sentence):
            if '/' in segment:
                parts = segment.split('/')
                processed_parts = []
                for k, part in enumerate(parts):
                    # Only first part considers sentence start if applicable
                    processed_parts.append(process_segment(part, is_first_word_of_sentence and k == 0))
                return "/".join(processed_parts)

            lower_segment = segment.lower()

            if segment.upper() in upper_case_words: # Check case-insensitive against upperCaseWords
                 return segment.upper()

            if segment.upper() in roman_numerals:
                 return segment.upper()

            for p in special_prefixes:
                if lower_segment.startswith(p):
                     if len(lower_segment) > len(p):
                         rest = lower_segment[len(p):]
                         return p + rest.capitalize()
                     else:
                         if is_first_word_of_sentence:
                             return lower_segment.capitalize()
                         else:
                             return lower_segment

            if is_first_word_of_sentence:
                return lower_segment.capitalize()

            if lower_segment in lower_case_words:
                return lower_segment

            return lower_segment.capitalize()

        transformed_core = process_segment(core, index == 0)
        processed_words.append(prefix + transformed_core + suffix)

    return " ".join(processed_words)

def process_file(filepath):
    print(f"Processing {filepath}...")
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
        return

    modified = False

    for ancestry in data:
        if 'habilidades' in ancestry:
            for ability in ancestry['habilidades']:
                original_name = ability.get('nome', '')
                new_name = to_fancy_title_case(original_name)

                # Special fix for cases like "Mordida/Garras" which might be in all caps "MORDIDA/GARRAS"
                # The logic handles '/' recursion, so "MORDIDA/GARRAS" -> "Mordida/Garras"

                if original_name != new_name:
                    ability['nome'] = new_name
                    modified = True
                    # print(f"  Changed: '{original_name}' -> '{new_name}'")

    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"Saved {filepath}")
    else:
        print(f"No changes in {filepath}")

def main():
    assets_dir = 'app/src/main/assets'
    # List of known ancestry files
    files = [f for f in os.listdir(assets_dir) if f.endswith('_ancestralidades.json')]

    for filename in files:
        filepath = os.path.join(assets_dir, filename)
        process_file(filepath)

if __name__ == '__main__':
    main()
