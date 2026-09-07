# Chapter 5: Function & Return Statements — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("BASIC FUNCTION WITH RETURN")
print("=" * 50)

def add(a, b):
    return a + b

print("add(3,4) =", add(3, 4))

print()
print("=" * 50)
print("MULTIPLE EARLY RETURNS")
print("=" * 50)

def describe_grade(score):
    if score >= 90:
        return "A"      # early exit
    elif score >= 80:
        return "B"
    elif score >= 70:
        return "C"
    else:
        return "F"

for s in [95, 82, 70, 40]:
    print(f"{s} -> {describe_grade(s)}")

print()
print("=" * 50)
print("RETURNING A TUPLE (multiple values)")
print("=" * 50)

def min_max(nums):
    return min(nums), max(nums)

low, high = min_max([3, 1, 4, 1, 5])
print("min:", low, "max:", high)

print()
print("=" * 50)
print("WITH vs WITHOUT RETURN (None)")
print("=" * 50)

def with_return(x):
    return x * 2

def without_return(x):
    x * 2   # no return -> returns None

print("with_return(5):", with_return(5))
print("without_return(5):", without_return(5))

print()
print("=" * 50)
print("PARAMETERS vs ARGUMENTS")
print("=" * 50)

def greet(name):          # `name` = parameter
    return f"Hello, {name}!"

print(greet("Alice"))     # "Alice" = argument
