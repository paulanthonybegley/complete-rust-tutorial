# Chapter 6: Value vs Reference — Main Examples
# Run:
#   python3 main.py

print("=" * 50)
print("VALUE SEMANTICS: numbers & strings are copied")
print("=" * 50)

a = 10
b = a          # COPY: b is independent
b = 20
print("a =", a)   # 10
print("b =", b)   # 20  (changing b did NOT affect a)

print()
print("=" * 50)
print("REFERENCE SEMANTICS: lists share the same data")
print("=" * 50)

original = [1, 2, 3]
copy = original          # NOT a copy! same underlying list
copy.append(4)           # mutates the shared list
print("original =", original)   # [1, 2, 3, 4]  ⚠️ changed!
print("copy     =", copy)

print()
print("=" * 50)
print("GETTING A TRUE COPY WITH .copy()")
print("=" * 50)

original = [1, 2, 3]
real_copy = original.copy()   # independent
real_copy.append(4)
print("original  =", original)   # [1, 2, 3]  unchanged
print("real_copy =", real_copy)  # [1, 2, 3, 4]

print()
print("=" * 50)
print("FUNCTIONS: numbers by value, lists by reference")
print("=" * 50)

def change_val(x):
    x = 100          # rebinding a number: only local

def change_list(lst):
    lst.append(999)  # mutating a list: affects caller

n = 5
change_val(n)
print("n after change_val =", n)          # 5 (value)

data = [1, 2, 3]
change_list(data)
print("data after change_list =", data)   # [1, 2, 3, 999] (reference)
