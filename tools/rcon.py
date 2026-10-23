# -*- coding: utf-8 -*-
"""Минимальный RCON-клиент: отправить команды на сервер и напечатать ответ.

Нужен, чтобы корректно завершить `gradle runServer` (stop → сохранение мира)
и не убивать процесс, теряя чанки.

Запуск:
    python tools/rcon.py <port> <password> save-all flush
    python tools/rcon.py <port> <password> stop
"""
from __future__ import annotations

import socket
import struct
import sys


def packet(req_id: int, ptype: int, body: str) -> bytes:
    data = struct.pack("<ii", req_id, ptype) + body.encode("utf-8") + b"\x00\x00"
    return struct.pack("<i", len(data)) + data


def recv_packet(sock: socket.socket) -> tuple[int, int, str]:
    raw_len = b""
    while len(raw_len) < 4:
        chunk = sock.recv(4 - len(raw_len))
        if not chunk:
            raise ConnectionError("сервер закрыл соединение")
        raw_len += chunk
    (length,) = struct.unpack("<i", raw_len)
    body = b""
    while len(body) < length:
        chunk = sock.recv(length - len(body))
        if not chunk:
            raise ConnectionError("сервер закрыл соединение")
        body += chunk
    req_id, ptype = struct.unpack("<ii", body[:8])
    return req_id, ptype, body[8:-2].decode("utf-8", "replace")


def main() -> int:
    if len(sys.argv) < 4:
        print(__doc__)
        return 2
    port = int(sys.argv[1])
    password = sys.argv[2]
    commands = sys.argv[3:]

    with socket.create_connection(("127.0.0.1", port), timeout=30) as sock:
        sock.sendall(packet(1, 3, password))
        req_id, _, _ = recv_packet(sock)
        if req_id == -1:
            print("RCON: неверный пароль")
            return 1
        for command in commands:
            sock.sendall(packet(2, 2, command))
            _, _, response = recv_packet(sock)
            print(f"$ {command}\n{response.strip()}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
