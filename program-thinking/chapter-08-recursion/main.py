# Chapter 8: Recursion — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("COUNTDOWN (base case stops recursion)")
print("=" * 50)

def countdown(n):
    if n == 0:                 # base case
        print("Blast off!")
        return
    print(n, end=" ")
    countdown(n - 1)           # recursive case

countdown(3)
print()

print()
print("=" * 50)
print("FACTORIAL")
print("=" * 50)

def factorial(n):
    if n == 1:                 # base case
        return 1
    return n * factorial(n - 1)   # recursive case

print("factorial(5) =", factorial(5))   # 120

print()
print("=" * 50)
print("SUM A LIST RECURSIVELY")
print("=" * 50)

def sum_list(nums):
    if not nums:               # base case: empty -> 0
        return 0
    return nums[0] + sum_list(nums[1:])   # first + recurse on the rest

print("sum_list([1,2,3,4,5]) =", sum_list([1, 2, 3, 4, 5]))   # 15

print()
print("=" * 50)
print("FIBONACCI (with memoization to avoid slowness)")
print("=" * 50)

def fib(n, memo=None):
    if memo is None:
        memo = {}
    if n in memo:
        return memo[n]
    if n <= 1:                 # base cases
        return n
    result = fib(n - 1, memo) + fib(n - 2, memo)
    memo[n] = result
    return result

print([fib(i) for i in range(10)])   # [0,1,1,2,3,5,8,13,21,34]

print()
print("=" * 50)
print("NO BASE CASE -> RecursionError")
print("=" * 50)

def no_base(n):
    return no_base(n - 1)      # never stops!

try:
    no_base(10)
except RecursionError as e:
    print("RecursionError:", e)
