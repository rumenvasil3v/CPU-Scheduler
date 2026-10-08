def factorial(x):
    if x == 1 or x == 0:
        return 1
    else:
        return (x * factorial(x-1))

num = 7
result = factorial(num)
def hcf(x, y):
    # Find HCF using Euclidean algorithm
    while y:
        x, y = y, x % y
    return x

def compute_lcm(x, y):
    # LCM = (x * y) / HCF
    return (x * y) // hcf(x, y)
    
#print("The factorial of", num, "is", result)


print("HCF: " + str(hcf(64, 36)) + ", LCM: " + str(compute_lcm(64, 36)))