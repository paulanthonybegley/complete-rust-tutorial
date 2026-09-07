# Chapter 2: If, Else & Elif — Exercise
# Complete the TODOs. Run with:
#   python3 exercise.py

print("Exercise: Control Flow")

# TODO 1: Write a program that prints POSITIVE, ZERO, or NEGATIVE
# based on a number. Test with 5, 0, and -3.

# TODO 2: Leap year function.
# A year is a leap year if divisible by 4, except years divisible by 100
# unless they're also divisible by 400.
# Example: 2000 -> True, 1900 -> False, 2024 -> True
def is_leap(year):
    # TODO implement
    return False

for y in [2000, 1900, 2024, 2023]:
    print(f"{y} is leap: {is_leap(y)}")

# TODO 3: Predict the output of this BEFORE running, then uncomment to check.
# x = 5
# if x > 3: print("P")
# if x > 2: print("Q")
# else: print("R")
