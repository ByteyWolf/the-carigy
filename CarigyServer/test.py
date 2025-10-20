import socket
import struct
import pygame
import numpy as np

HOST = '0.0.0.0'
PORT = 1723
WIDTH, HEIGHT = 160, 120

def decode_556(byte_array):
    pixels = []
    for b in byte_array:
        r = (b >> 5) & 0x07
        g = (b >> 2) & 0x07
        b_ = b & 0x03
        r = int(r / 7 * 255)
        g = int(g / 7 * 255)
        b_ = int(b_ / 3 * 255)
        pixels.append((r, g, b_))
    return pixels

def main():
    pygame.init()
    screen = pygame.display.set_mode((WIDTH, HEIGHT))
    pygame.display.set_caption("J2ME Video Stream")
    clock = pygame.time.Clock()

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.bind((HOST, PORT))
        s.listen(1)
        print(f"Listening on {HOST}:{PORT}")
        conn, addr = s.accept()
        with conn:
            print("Connected by", addr)
            running = True
            while running:
                for event in pygame.event.get():
                    if event.type == pygame.QUIT:
                        running = False

                len_bytes = conn.recv(8)
                if len(len_bytes) < 8:
                    break
                frame_len = struct.unpack('>Q', len_bytes)[0]

                frame_data = b''
                while len(frame_data) < frame_len:
                    chunk = conn.recv(frame_len - len(frame_data))
                    if not chunk:
                        break
                    print(f"got {len(chunk)}")
                    frame_data += chunk
                print(f"Wonderful: {len(frame_data)}/{frame_len}")
                if len(frame_data) != frame_len:
                    break

                pixels = decode_556(frame_data)
                surf = pygame.Surface((WIDTH, HEIGHT))
                pygame.surfarray.blit_array(surf, 
                    pygame.surfarray.map_array(pygame.Surface((WIDTH, HEIGHT)), 
                    pixels_to_array(pixels, WIDTH, HEIGHT)))
                screen.blit(surf, (0,0))
                pygame.display.flip()
                clock.tick(30)

    pygame.quit()

def pixels_to_array(pixels, width, height):
    arr = np.zeros((width, height, 3), dtype=np.uint8)
    for y in range(height):
        for x in range(width):
            arr[x, y] = pixels[y*width + x]
    return arr

if __name__ == '__main__':
    main()
