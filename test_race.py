import threading
import urllib.request
import urllib.error
import json

URL = "http://localhost:8081/api/posts/1/comments"
TOTAL_REQUESTS = 200

success = 0
rejected = 0
lock = threading.Lock()

def send_request(bot_id):
    global success, rejected
    payload = {
        "authorId": bot_id,
        "authorType": "BOT",
        "content": f"Bot {bot_id} comment",
        "depthLevel": 1
    }
    data = json.dumps(payload).encode("utf-8")
    request = urllib.request.Request(
        URL,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    try:
        with urllib.request.urlopen(request) as response:
            status_code = response.getcode()
        with lock:
            if status_code == 201:
                success += 1
            else:
                rejected += 1
    except urllib.error.HTTPError:
        with lock:
            rejected += 1
    except urllib.error.URLError as e:
        print(f"Error: {e}")

# Create 200 threads (200 bots)
threads = []
for i in range(1, TOTAL_REQUESTS + 1):
    t = threading.Thread(target=send_request, args=(i,))
    threads.append(t)

# Fire all at same time
for t in threads:
    t.start()

for t in threads:
    t.join()

print(f"\n✅ Accepted: {success}")
print(f"❌ Rejected: {rejected}")
print(f"📊 Total: {success + rejected}")