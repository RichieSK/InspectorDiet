import pymongo


    #     totalNutritions[nutrient] += foodDoc[nutrient]

def get_nutrition(foods):
    client = pymongo.MongoClient()
    db = client['food']
    collection = db['nutrition']


    selectedNutrition = {
        "_id" : 0,
        "serving size" : 0,
        "name" : 0,
        "unit" : 0,
    }

    totalNutritions = {}

    for food in foods:
        food = food.lower()
        foodDoc = collection.find_one(filter = {"name" : food}, projection = selectedNutrition)
        if foodDoc == None:
            return {}
        for nutrient in foodDoc:
            qnt = foodDoc[nutrient].split()[0]
            # while qnt[-1].isalpha():
            #     qnt = qnt[:-1]
            print(nutrient, qnt)
            try:
                totalNutritions[nutrient] += float(qnt)
            except:
                totalNutritions[nutrient] = float(qnt)

    collection = db['units']
    units = collection.find(projection={"_id" : 0})
    for rec in units:
        totalNutritions[rec["name"]] = str(totalNutritions[rec["name"]]) + rec["metrics"]
    print(totalNutritions)
    return totalNutritions

def get_nutrition_each(foods):
    client = pymongo.MongoClient()
    db = client['food']
    collection = db['nutrition']

    selectedNutrition = {
        "_id" : 0,
        "serving size" : 0,
        "name" : 0,
        "unit" : 0,
    }

    totalNutritions = {}

    for food in foods:
        foodDictionary = {}
        food = food.lower()
        foodDoc = collection.find_one(filter = {"name" : food}, projection = selectedNutrition)
        if foodDoc == None:
            continue
        for nutrient in foodDoc:
            qnt = foodDoc[nutrient].split()[0]
            print(nutrient, qnt)
            foodDictionary[nutrient] = float(qnt)
        totalNutritions[food] = foodDictionary

    print(totalNutritions)
    return totalNutritions
    
def get_foods(exclude=None):
    client = pymongo.MongoClient()
    db = client['food']
    collection = db['nutrition']
    recs = collection.find(projection = {"_id" : 0, "name" : 1})
    lst = []
    for rec in recs:
        foodName = rec["name"]
        if type(exclude) == list:
            if foodName in exclude:
                continue
        lst.append(rec["name"])
    return lst

def get_calories_of_foods(dct):
    client = pymongo.MongoClient()
    db = client['food']
    collection = db['nutrition']
    l = []
    for foods in dct["foods"]:
        d = {}
        for food in foods:
            print("Searching", food)
            rec = collection.find_one(filter = {"name" : food},projection = {"_id" : 0, "name" : 0, "unit" : 0})
            
            print(rec)
            # for rec in recs:
            #     d[rec] = d[recs[rec]]
        l.append(d)
    print(l)


def get_units():
    client = pymongo.MongoClient()
    db = client['food']
    collection = db['units']
    selectRows = {
        "_id" : 0,
    }
    recs = collection.find(projection = selectRows)
    dct = {}
    for row in recs:
        dct[row["name"]] = row["metrics"]
    return dct

def add_to_user_db(rec):
    client = pymongo.MongoClient()
    db = client['food']
    collection = db['users']
    collection.insert_one(rec)

def get_user_history(user):
    client = pymongo.MongoClient()
    db = client['food']
    collection = db['users']
    recs = collection.find({"user" : user}, {"_id" : 0}).sort("effectiveTS", pymongo.DESCENDING).limit(5)

    dct = {"user" : user}
    foods = []
    for rec in recs:
        foods.append(rec["foods"])
    dct["foods"] = foods
    return dct

    