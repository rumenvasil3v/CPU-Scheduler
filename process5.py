import random
import math
import datetime

def generate_fibonacci(n):
    fib_sequence = [0, 1]
    while len(fib_sequence) < n:
        fib_sequence.append(fib_sequence[-1] + fib_sequence[-2])
    return fib_sequence

def is_prime(num):
    if num < 2:
        return False
    for i in range(2, int(math.sqrt(num)) + 1):
        if num % i == 0:
            return False
    return True

def analyze_list(numbers):
    prime_numbers = [num for num in numbers if is_prime(num)]
    even_numbers = [num for num in numbers if num % 2 == 0]
    odd_numbers = [num for num in numbers if num % 2 != 0]
    
    return {
        'total_count': len(numbers),
        'prime_count': len(prime_numbers),
        'prime_numbers': prime_numbers,
        'even_count': len(even_numbers),
        'odd_count': len(odd_numbers),
        'max_value': max(numbers),
        'min_value': min(numbers),
        'average': sum(numbers) / len(numbers)
    }

def simulate_random_walk(steps):
    x, y = 0, 0
    walk_path = [(x, y)]
    
    for _ in range(steps):
        direction = random.choice(['N', 'S', 'E', 'W'])
        if direction == 'N':
            y += 1
        elif direction == 'S':
            y -= 1
        elif direction == 'E':
            x += 1
        else:
            x -= 1
        walk_path.append((x, y))
    
    return walk_path

def main():
    # Generate Fibonacci sequence
    fibonacci_sequence = generate_fibonacci(4)
    print("Fibonacci:", fibonacci_sequence)
    
    # Generate random list
    random_numbers = [random.randint(1, 100) for _ in range(50)]
    
    # Analyze list
    list_analysis = analyze_list(random_numbers)
    #print("\nList Analysis:")
    #for key, value in list_analysis.items():
    #    print(f"{key.replace('_', ' ').title()}: {value}")
    
    # Random walk simulation
    random_walk = simulate_random_walk(100)
    #print("\nRandom Walk Final Position:", random_walk[-1])
    
    # Generate timestamp
    current_time = datetime.datetime.now()
    # print("\nScript Execution Time:", current_time.strftime("%Y-%m-%d %H:%M:%S"))
    
    # Some mathematical calculations
    # print("\nMathematical Computations:")
    #print("Square Root of 16:", math.sqrt(16))
    #print("10 Factorial:", math.factorial(10))
    #print("Sine of π/2:", math.sin(math.pi/2))

if __name__ == "__main__":
    main()