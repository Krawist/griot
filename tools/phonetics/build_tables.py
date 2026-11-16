#!/usr/bin/env python3
"""Builds the compact phonetic tables of :core:phonetics from the dictionaries' original files.

Usage: build_tables.py <Lexique383.tsv> <cmudict.dict> <output folder>

- fr-lexique.tsv.gz: one line per written form, "word<TAB>phonemes" in Lexique's notation, sorted by word; for a form
  with several pronunciations (« est »), the most frequent one in the films subtitles.
- en-cmudict.tsv.gz: one line per word, "word<TAB>ARPAbet phonemes with stress", sorted by word; the first
  pronunciation of each word (the alternates « word(2) » are left out).
"""
import csv
import gzip
import sys


def build_french(source, target):
    best = {}
    with open(source, encoding="utf-8") as tsv:
        for row in csv.DictReader(tsv, delimiter="\t", quoting=csv.QUOTE_NONE):
            word, phon = row["ortho"].strip(), row["phon"].strip()
            if not word or not phon:
                continue
            freq = float(row["freqfilms2"] or 0) + float(row["freqlivres"] or 0)
            if word not in best or freq > best[word][1]:
                best[word] = (phon, freq)
    with gzip.open(target, "wt", encoding="utf-8") as out:
        for word in sorted(best):
            out.write(f"{word}\t{best[word][0]}\n")
    return len(best)


def build_english(source, target):
    words = {}
    with open(source, encoding="utf-8") as dic:
        for line in dic:
            line = line.split("#")[0].strip()
            if not line:
                continue
            word, phonemes = line.split(" ", 1)
            if "(" in word or word in words:
                continue
            words[word] = phonemes.strip()
    with gzip.open(target, "wt", encoding="utf-8") as out:
        for word in sorted(words):
            out.write(f"{word}\t{words[word]}\n")
    return len(words)


if __name__ == "__main__":
    lexique, cmu, folder = sys.argv[1:4]
    print("fr", build_french(lexique, f"{folder}/fr-lexique.tsv.gz"))
    print("en", build_english(cmu, f"{folder}/en-cmudict.tsv.gz"))
