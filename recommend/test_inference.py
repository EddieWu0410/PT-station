import requests

API_URL = "http://10.126.59.25:5000/recommend"

user_ids = ['user1', '550e8400-e29b-41d4-a716-446655440000']

for user_id in user_ids:
    try:
        response = requests.post(API_URL, json={'user_id': user_id})
        response.raise_for_status()
        result = response.json()
        print(f"Recommendations for user_id '{user_id}': {result}")
    except requests.exceptions.RequestException as e:
        print(f"Error occurred while fetching recommendations for user_id '{user_id}': {e}")
