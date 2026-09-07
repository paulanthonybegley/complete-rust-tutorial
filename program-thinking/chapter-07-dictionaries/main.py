# Chapter 7: Python Dictionary — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("CREATING AND ACCESSING A DICT")
print("=" * 50)

scores = {"Harry": 90, "Ron": 85, "Hermione": 100}
print("scores['Harry']:", scores["Harry"])

# Add / update
scores["Malfoy"] = 70
scores["Harry"] = 95
print("after add/update:", scores)

# Safe access with .get()
print("scores.get('Luna'):", scores.get("Luna"))      # None
print("scores.get('Luna', 0):", scores.get("Luna", 0)) # 0

print()
print("=" * 50)
print("REMOVING ITEMS (pop)")
print("=" * 50)

menu = {"coffee": 3.5, "tea": 2.5, "pastry": 4.0}
removed = menu.pop("tea")     # "Shamrock removed from the menu"
print("removed tea:", removed)
print("menu now:", menu)

print()
print("=" * 50)
print("NESTED DICTIONARIES (dict inside dict)")
print("=" * 50)

restaurants = {
    "Chick-fil-A": {"waffle fries": 2.49, "sandwich": 5.99},
    "Cafe":       {"coffee": 3.5, "tea": 2.5},
}
# Chained lookups
price = restaurants["Chick-fil-A"]["waffle fries"]
print("waffle fries price:", price)   # 2.49

print()
print("=" * 50)
print("KEY RESTRICTION: keys cannot be lists/dicts")
print("=" * 50)

try:
    bad = {["a"]: 1}     # TypeError
except TypeError as e:
    print("Tried a list key ->", e)
print("=" * 50)
print("ITERATING OVER A DICT")
print("=" * 50)

for name, score in scores.items():
    print(f"{name}: {score}")

print()
print("=" * 50)
print("REAL-WORLD: COUNT CHARACTERS IN A BOOK")
print("=" * 50)

paragraph = (
    "Harry met Ron and Hermione on the train. "
    "Harry and Ron became friends. "
    "Malfoy frowned at Harry and Hermione. "
    "Hermione helped Harry with homework."
)

counts = {"Harry": 0, "Ron": 0, "Hermione": 0, "Malfoy": 0}
for word in paragraph.replace(".", " ").split():
    if word in counts:
        counts[word] += 1
print(counts)

# More robust counting idiom (doesn't require pre-listing keys):
word_counts = {}
for word in paragraph.replace(".", " ").split():
    word_counts[word] = word_counts.get(word, 0) + 1
print("top entries:", word_counts)
