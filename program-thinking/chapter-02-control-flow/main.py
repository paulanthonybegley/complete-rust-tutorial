# Chapter 2: If, Else & Elif — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("BASIC IF / ELSE")
print("=" * 50)

age = 15
if age >= 18:
    print("adult")
else:
    print("minor")   # 15 < 18 -> prints

print()
print("=" * 50)
print("ELIF CHAIN (first match wins)")
print("=" * 50)

def grade(score):
    if score >= 90:
        return "A"
    elif score >= 80:
        return "B"
    elif score >= 70:
        return "C"
    else:
        return "F"

for s in [95, 82, 75, 40]:
    print(f"score={s} -> grade {grade(s)}")

print()
print("=" * 50)
print("LOGICAL OPERATORS")
print("=" * 50)

score, age2 = 85, 20
if score >= 80 and age2 < 30:
    print("high score from a young player")

if score >= 90 or score >= 85:
    print("at least 85")

flag = True
if not flag:
    print("flag is False")
else:
    print("flag is True")

print()
print("=" * 50)
print("TWO SEPARATE `if` STATEMENTS VS ONE `if/elif`")
print("=" * 50)

x = 5
if x > 3:
    print("A (independent if - runs)")
if x > 2:
    print("B (independent if - runs too)")
else:
    print("C")

# Compare: elif chain - only ONE runs
if x > 3:
    print("D")
elif x > 2:
    print("E")
else:
    print("F")
