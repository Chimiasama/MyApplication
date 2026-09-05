#!/usr/bin/env python3
"""Convert an Android Lint XML report into a Markdown summary.

Usage: lint_summary.py <lint-results.xml> [<lint-results.xml> ...]

Prints a Markdown table (one row per warning/issue) plus a counts-by-id
summary to stdout, suitable for appending to $GITHUB_STEP_SUMMARY.
"""
import sys
import xml.etree.ElementTree as ET
from collections import Counter


def escape(text: str) -> str:
    if text is None:
        return ""
    return (
        text.replace("|", "\\|")
        .replace("\n", " ")
        .replace("\r", " ")
        .strip()
    )


def main(paths: list[str]) -> int:
    issues = []
    for path in paths:
        try:
            tree = ET.parse(path)
        except (ET.ParseError, FileNotFoundError):
            continue
        root = tree.getroot()
        for issue in root.findall("issue"):
            location = issue.find("location")
            issues.append(
                {
                    "id": issue.get("id", ""),
                    "severity": issue.get("severity", ""),
                    "category": issue.get("category", ""),
                    "message": issue.get("message", ""),
                    "file": location.get("file", "") if location is not None else "",
                    "line": location.get("line", "") if location is not None else "",
                }
            )

    if not issues:
        print("Nenhum warning de lint encontrado. ✅")
        return 0

    severity_counts = Counter(i["severity"] for i in issues)
    id_counts = Counter(i["id"] for i in issues)

    print(f"**Total de ocorrências:** {len(issues)}")
    print()
    print("| Severidade | Quantidade |")
    print("|---|---|")
    for severity, count in sorted(severity_counts.items(), key=lambda kv: -kv[1]):
        print(f"| {severity} | {count} |")
    print()

    print("<details><summary>Top regras (por quantidade)</summary>")
    print()
    print("| Regra | Quantidade |")
    print("|---|---|")
    for rule_id, count in id_counts.most_common(30):
        print(f"| `{rule_id}` | {count} |")
    print()
    print("</details>")
    print()

    print("<details><summary>Lista completa de warnings</summary>")
    print()
    print("| Severidade | Regra | Arquivo | Linha | Mensagem |")
    print("|---|---|---|---|---|")
    for i in sorted(issues, key=lambda x: (x["file"], int(x["line"] or 0))):
        file_short = i["file"].split("/app/")[-1] if "/app/" in i["file"] else i["file"]
        print(
            f"| {escape(i['severity'])} | `{escape(i['id'])}` | "
            f"`{escape(file_short)}` | {escape(i['line'])} | {escape(i['message'])} |"
        )
    print()
    print("</details>")
    return 0


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Uso: lint_summary.py <lint-results.xml> [...]", file=sys.stderr)
        sys.exit(1)
    sys.exit(main(sys.argv[1:]))
