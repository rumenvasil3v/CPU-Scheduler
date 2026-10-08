def calculate_square_and_cube(number):
    square = number ** 2
    cube = number ** 3
    
    
a = [10, 20, 30, 40, 50, 60, 70]

# Elements to remove
remove = [20, 40, 60]

# Remove elements using a simple for loop
res = []

for val in a:
    if val not in remove:
        res.append(val)

print(res)