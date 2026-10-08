def hcf(x, y):
    """Calculate Highest Common Factor using Euclidean algorithm."""
    while y:
        x, y = y, x % y
    return x

def compute_lcm(x, y):
    """Calculate Least Common Multiple using HCF method."""
    return (x * y) // hcf(x, y)

def prime_factors(n):
    """Compute prime factors of a given number."""
    factors = []
    d = 2
    while n > 1:
        while n % d == 0:
            factors.append(d)
            n //= d
        d += 1
        if d * d > n:
            if n > 1:
                factors.append(n)
            break
    return factors

def is_coprime(x, y):
    """Check if two numbers are coprime (HCF is 1)."""
    return hcf(x, y) == 1

def number_operations(numbers):
    """Perform various operations on a list of numbers."""
    results = {}
    for num in numbers:
        results[num] = {
            'prime_factors': prime_factors(num),
            'is_perfect_square': int(num**0.5)**2 == num
        }
    return results

def main():
    """Main function to demonstrate number-related calculations."""
    x, y = 64, 36
    numbers_to_analyze = [x, y, 100, 289, 17]
    
    #print(f"HCF of {x} and {y}: {hcf(x, y)}")
    #print(f"LCM of {x} and {y}: {compute_lcm(x, y)}")
    #print(f"Are {x} and {y} coprime? {is_coprime(x, y)}")
    
    analysis = number_operations(numbers_to_analyze)
    #print("\nNumber Analysis:")
    #for num, details in analysis.items():
    print("Number Analysis:", len(analysis))



if __name__ == "__main__":
    main()