# Chapter 8: Recursion — Exercise
# Complete the TODOs. Run with:
#   python3 exercise.py

print("Exercise: Recursion")

# TODO 1: Write a recursive `count_up(n)` that prints 1..n,
# using a base case n==0 and recursion. Hint: count_up(n-1) FIRST,
# then print n, so numbers come out increasing.
def count_up(n):
    if n == 0:
        return
    count_up(n - 1)
    print(n, end=" ")

count_up(5)
print()

# TODO 2: Write a recursive `power(base, exp)` returning base ** exp.
# base case: power(b, 0) == 1. Recursive: b * power(b, exp-1).
def power(base, exp):
    return 0  # TODO

print("power(2, 8) =", power(2, 8))   # 256

# TODO 3: Write a recursive `count_len(lst)` that returns the number
# of items WITHOUT using len(). Base: empty -> 0. Recursive: 1 + count_len(rest).
def count_len(lst):
    return 0  # TODO

print("count_len([10,20,30,40]) =", count_len([10, 20, 30, 40]))   # 4

# TODO 4: Trace factorial(3) by hand. Write the sequence of calls
# and the unwound results as a comment.

# TODO 5 (thinking): Why would the Fibonnaci function WITHOUT memoization
# be extremely slow? Write your answer as a comment.
