#!/usr/bin/env python3
"""Builds the compact phonetic tables of :core:phonetics from the dictionaries' original files.

Usage: build_tables.py <Lexique383.tsv> <cmudict.dict> <en_50k.txt> <output folder>

- fr-lexique.tsv.gz: one line per written form, "word<TAB>phonemes" in Lexique's notation, sorted by word; for a form
  with several pronunciations (« est »), the most frequent one in the films subtitles.
- fr-frequent.txt.gz: the most frequent single words of Lexique (films subtitles and books), one per line, most
  frequent first: the rhyme suggestions show common words first.
- en-cmudict.tsv.gz: one line per word, "word<TAB>ARPAbet phonemes with stress", sorted by word; the first
  pronunciation of each word (the alternates « word(2) » are left out).
- en-frequent.txt.gz: the words of en_50k.txt (FrequencyWords, OpenSubtitles 2018) that the CMU dictionary has, most
  frequent first: the CMU dictionary has no frequencies.
"""
import csv
import gzip
import sys


FREQUENT_WORDS = 40_000


def build_french(source, target, frequent_target):
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
    frequent = sorted((word for word in best if " " not in word and best[word][1] > 0), key=lambda w: -best[w][1])
    with gzip.open(frequent_target, "wt", encoding="utf-8") as out:
        for word in frequent[:FREQUENT_WORDS]:
            out.write(f"{word}\n")
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
    return words


def build_english_frequent(source, words, target):
    count = 0
    with open(source, encoding="utf-8") as freq, gzip.open(target, "wt", encoding="utf-8") as out:
        for line in freq:
            word = line.split(" ", 1)[0].strip()
            if word in words:
                out.write(f"{word}\n")
                count += 1
    return count


if __name__ == "__main__":
    lexique, cmu, frequencies, folder = sys.argv[1:5]
    print("fr", build_french(lexique, f"{folder}/fr-lexique.tsv.gz", f"{folder}/fr-frequent.txt.gz"))
    english = build_english(cmu, f"{folder}/en-cmudict.tsv.gz")
    print("en", len(english))
    print("en frequent", build_english_frequent(frequencies, english, f"{folder}/en-frequent.txt.gz"))
