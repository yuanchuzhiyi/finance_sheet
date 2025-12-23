from __future__ import annotations

import json
import sqlite3
from pathlib import Path
from typing import Any, Dict

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

DB_PATH = Path(__file__).resolve().parent / "report.db"


def init_db() -> None:
    DB_PATH.parent.mkdir(parents=True, exist_ok=True)
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS report_store (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                payload TEXT NOT NULL
            )
            """
        )
        conn.commit()


def get_payload() -> Dict[str, Any]:
    with sqlite3.connect(DB_PATH) as conn:
        cur = conn.execute("SELECT payload FROM report_store WHERE id = 1")
        row = cur.fetchone()
        if not row:
            return {}
        try:
            return json.loads(row[0])
        except json.JSONDecodeError:
            return {}


def set_payload(data: Dict[str, Any]) -> None:
    payload = json.dumps(data, ensure_ascii=False)
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute(
            "INSERT INTO report_store (id, payload) VALUES (1, ?) "
            "ON CONFLICT(id) DO UPDATE SET payload = excluded.payload",
            (payload,),
        )
        conn.commit()


class ReportBody(BaseModel):
    data: Dict[str, Any]


init_db()

app = FastAPI(title="Family Finance API", version="1.0.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/report")
def read_report() -> Dict[str, Any]:
    return {"data": get_payload()}


@app.put("/report")
def write_report(body: ReportBody) -> Dict[str, str]:
    try:
        set_payload(body.data)
    except Exception as exc:  # pragma: no cover - minimal API
        raise HTTPException(status_code=500, detail=str(exc))
    return {"status": "ok"}


@app.delete("/report")
def delete_report() -> Dict[str, str]:
    with sqlite3.connect(DB_PATH) as conn:
        conn.execute("DELETE FROM report_store WHERE id = 1")
        conn.commit()
    return {"status": "deleted"}
