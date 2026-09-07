# Chapter 0: Why Programming Thinking — Examples
# Run:
#   python3 examples.py

print("=" * 50)
print("THE 'VIBE CODER' vs 'PROGRAMMING THINKER'")
print("=" * 50)

# A "vibe coder" might ask AI: "build me a menu"
# A "programming thinker" first thinks about the DATA MODEL:

# What are the building blocks?
drink = "coffee"          # a variable (string)
price = 3.5               # a variable (float)
in_stock = True           # a variable (boolean)

# The thinker organizes data before writing ANY code:
menu = [                  # a list (collection of items)
    ("coffee", 3.5),
    ("tea", 2.5),
    ("pastry", 4.0),
]

print("Data model for a menu:")
for item in menu:
    print(f"  {item[0]}: ${item[1]:.2f}")

print()
print("Went from 'asking for code' to 'specifying a data model'.")
print("This is programming thinking in action.")
