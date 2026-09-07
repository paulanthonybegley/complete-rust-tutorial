# Chapter 3: Python List — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("CREATING AND INDEXING LISTS")
print("=" * 50)

grocery_list = ["milk", "eggs", "bread"]
scores = [98, 85, 72, 90]
mixed = [1, "hello", 3.5, True]
empty = []

print("grocery_list:", grocery_list)
print("scores[0]:", scores[0])       # 98
print("scores[-1]:", scores[-1])     # 90
print("scores[1:3]:", scores[1:3])   # [85, 72]
print("mixed:", mixed)
print("empty:", empty)

print()
print("=" * 50)
print("MUTATION OPERATIONS")
print("=" * 50)

menu = ["coffee", "tea", "pastry"]
print("original:", menu)
menu.append("cake")                       # add to end
print("after append:", menu)
menu.insert(1, "juice")                   # insert at index 1
print("after insert:", menu)
menu.remove("tea")                        # remove by value
print("after remove:", menu)
last = menu.pop()                         # remove & return last
print("popped:", last, "| now:", menu)
print("length:", len(menu))

print()
print("=" * 50)
print("THE MENTAL MODEL: SIMPLE DATA vs LISTS")
print("=" * 50)

number = 7            # data stored directly "in" the variable
my_list = [1, 2]      # variable stores a REFERENCE/arrow to the list

print("number:", number)
print("my_list:", my_list)
print("A list variable holds an 'arrow', NOT a copy of the whole list.")
print("(We explore WHY in Chapter 6 — Value vs Reference)")
