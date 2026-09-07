# Chapter 1: Variable — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("ASSIGNMENT IS A VERB, NOT A NOUN")
print("=" * 50)

# `=` is an ACTION: "bind this name to this value at this moment"
x = 5
print("x =", x)

# `x = x + 1` is valid in programming (nonsense in math)
x = x + 1
x = x + 1
print("after x = x + 1 twice, x =", x)   # 7

print()
print("=" * 50)
print("VARIABLES ARE FLEXIBLE (DYNAMIC TYPES)")
print("=" * 50)

thing = 5
print("thing holds an int:      ", thing, type(thing))
thing = "hello"
print("thing now holds a string:", thing, type(thing))
thing = [1, 2, 3]
print("thing now holds a list:  ", thing, type(thing))

print()
print("=" * 50)
print("A NAME CAN BE REBOUND; THE OLD VALUE IS REPLACED")
print("=" * 50)

temperature = 20
print("morning temperature:", temperature)
temperature = 25
print("noon temperature:   ", temperature)

print()
print("=" * 50)
print("PREVIEW: SIMPLE DATA vs LISTS (references)")
print("=" * 50)

number = 7          # simple value lives "in" the variable
my_list = [1, 2]    # the variable stores a REFERENCE/arrow to the list
print("number:", number)          # direct value
print("my_list:", my_list)        # actually an arrow to the data
print("(Chapter 6 explores this in depth)")
