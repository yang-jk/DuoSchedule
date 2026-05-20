#!/usr/bin/env python3
import re
import sys

CHANGELOG_FILE = "Product-Spec-CHANGELOG.md"
OUTPUT_FILE = "app/src/main/java/com/duoschedule/ui/settings/ChangelogData.kt"

TYPE_MAP = {
    "功能增强": "FEATURE",
    "功能优化": "FEATURE",
    "性能优化": "FEATURE",
    "Bug修复": "BUGFIX",
    "Bug 修复": "BUGFIX",
    "重大变更": "BREAKING",
}

def parse_changelog(filepath):
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    entries = []
    pattern = re.compile(
        r'##\s*\[(\d+\.\d+\.\d+)\]\s*-\s*(\d{4}-\d{2}-\d{2})\s*\n'
        r'(.*?)(?=\n##\s*\[|\n##\s*版本号|\Z)',
        re.DOTALL
    )

    for match in pattern.finditer(content):
        version = match.group(1)
        date = match.group(2)
        body = match.group(3)

        change_type = "FEATURE"
        type_match = re.search(r'###\s*变更类型[：:]\s*(.+)', body)
        if type_match:
            raw_type = type_match.group(1).strip()
            change_type = TYPE_MAP.get(raw_type, "FEATURE")

        summary = ""
        summary_match = re.search(r'\*\*(.+?)\*\*', body)
        if summary_match:
            summary = summary_match.group(1).strip().replace('"', '\\"')
        else:
            summary = f"版本 {version} 更新"

        entries.append({
            "version": version,
            "date": date,
            "type": change_type,
            "summary": summary,
        })

    return entries

def generate_kotlin(entries):
    lines = []
    lines.append("package com.duoschedule.ui.settings")
    lines.append("")
    lines.append("val changelogEntries = listOf(")
    for entry in entries:
        lines.append(f'    ChangelogEntry(')
        lines.append(f'        version = "{entry["version"]}",')
        lines.append(f'        date = "{entry["date"]}",')
        lines.append(f'        type = ChangelogType.{entry["type"]},')
        lines.append(f'        summary = "{entry["summary"]}"')
        lines.append(f'    ),')
    lines.append(")")

    return "\n".join(lines)

if __name__ == "__main__":
    entries = parse_changelog(CHANGELOG_FILE)
    if not entries:
        print(f"Warning: No changelog entries found in {CHANGELOG_FILE}", file=sys.stderr)
        sys.exit(1)

    kotlin_code = generate_kotlin(entries)
    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        f.write(kotlin_code)

    print(f"Generated {OUTPUT_FILE} with {len(entries)} entries")
