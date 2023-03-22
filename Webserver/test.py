import requests

def test_upload_file():
    url = 'http://localhost:8000/myimage/'
    imgpath = './img1.jpg'
    files = {'sentFile': open(imgpath, 'rb')}
    response = requests.post(url, files=files)
    if response.status_code == 200:
        print('File uploaded successfully')
        print('File URL:', response.json())
    else:
        print('Error:', response.text)

test_upload_file()