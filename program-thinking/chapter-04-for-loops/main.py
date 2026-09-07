# Chapter 4: For Loop — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("FOR OVER A LIST")
print("=" * 50)

fruits = ["apple", "banana", "cherry"]
for fruit in fruits:
    print(fruit)

print()
print("=" * 50)
print("FOR OVER RANGE")
print("=" * 50)

print("range(5):")
for i in range(5):
    print(i, end=" ")
print()

print("range(1, 6):")
for i in range(1, 6):
    print(i, end=" ")
print()

print("range(0, 10, 2):")
for i in range(0, 10, 2):
    print(i, end=" ")
print()

print()
print("=" * 50)
print("ENUMERATE (index + value)")
print("=" * 50)

menu = ["coffee", "tea", "pastry"]
for index, item in enumerate(menu):
    print(index, item)

print()
print("=" * 50)
print("NESTED LOOPS + MULTIPLICATION TABLE")
print("=" * 50)

for row in range(1, 4):
    for col in range(1, 4):
        print(f"{row*col:2}", end=" ")
    print()

print("total iterations of inner loop (3x3):", 3 * 3)

print()
print("=" * 50)
print("BREAK AND CONTINUE")
print("=" * 50)

for i in range(10):
    if i == 3:
        continue
    if i == 7:
        break
    print(i, end=" ")
print()
