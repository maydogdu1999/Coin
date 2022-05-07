import random
n = random.randint(0, 1000000)
counter = 0
print("started")
while (counter < 40):
    if (random.randint(0,1000000) == n):
        counter += 1
        

print("Reached")