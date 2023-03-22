import pymongo

client = pymongo.MongoClient()
db = client['food']
collection = db['nutrition']

# selectedNutrition = {
#     "serving_size" : 1,
#     "calories" : 1,
#     "total_fat" : 1,
#     "folate" : 1,
#     "vitamin_a" : 1,
#     "vitamin_d" : 1,
#     "zink" : 1,
#     "sodium" : 1
# }
# documents = list(collection.find(projection = selectedNutrition).limit(5))
# print(documents)


nutrients = ["calories", "total_fat", "folate", "vitamin_a", "vitamin_d", "zink", "sodium"]

selectedNutrition = {
    "_id" : 0,
    # "serving_size" : 0,
}
for n in nutrients:
    selectedNutrition[n] = 1

totalNutritions = {}
for n in nutrients:
    totalNutritions[n] = 0

foods = ["Bananas, raw", "Oil, mustard"]
for food in foods:
    foodDoc = collection.find_one(filter = {"name" : food}, projection = selectedNutrition)
    for nutrient in foodDoc:
        qnt = foodDoc[nutrient].split()[0]
        print(nutrient, qnt)
        totalNutritions[nutrient] += float(qnt)

        # print(nutrient, foodDoc[nutrient])
print(totalNutritions)