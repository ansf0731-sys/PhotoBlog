
import socket
from datetime import datetime

HOST = "127.0.0.1"
PORT = 5001

def main():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server:
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind((HOST, PORT))
        server.listen(5)

        print(f"Socket Server started at {HOST}:{PORT}")
        print("Waiting for client requests...")

        while True:
            conn, addr = server.accept()
            with conn:
                print(f"\nRequest received at {datetime.now()}")
                print(f"Client address: {addr}")

                data = conn.recv(4096)
                if data:
                    message = data.decode("utf-8", errors="replace")
                    print(f"Received message: {message}")
                    conn.sendall(b"Request received successfully")

if __name__ == "__main__":
    main()
