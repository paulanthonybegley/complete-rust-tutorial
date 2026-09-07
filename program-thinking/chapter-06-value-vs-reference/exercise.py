# Chapter 6: Value vs Reference — Exercise
# Complete the TODOs. Run with:
#   python3 exercise.py

print("Exercise: Value vs Reference")

# TODO 1: Predict, then verify: does `y = x; y = 6` affect x for numbers?
x = 5
y = x
y = 6
print("x =", x, "(should be 5)")

# TODO 2: Here `b` and `a` share a list. What does a become after b.append?
a = [1, 2, 3]
b = a
b.append(99)
print("a =", a)   # ???  write your answer before running

# TODO 3: FIX the aliasing below so that b is independent.
alist = [1, 2, 3]
blist = alist          # TODO: change to a true copy
blist.append(7)
print("alist =", alist)   # should print [1, 2, 3] if fixed

# TODO 4: This function SHOULD remove negatives from a shared list.
# Because lists pass by reference, it mutates the caller's list.
def remove_negatives(nums):
    return [n for n in nums if n >= 0]

data = [1, -2, 3, -4, 5]
print("before:", data)
print("returned:", remove_negatives(data))
print("data after (untouched?):", data)

# TODO 5 (thought): Write a function that INADVERTENTLY modifies the
# caller's list (to prove you understand reference semantics).
