from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from openai import OpenAI
import os
from dotenv import load_dotenv
import json

# 1. 환경 변수 로드
load_dotenv()
API_KEY = os.getenv("OPENAI_API_KEY")

# SSAFY GMS 설정
client = OpenAI(
    api_key=API_KEY,
    base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
)

app = FastAPI()


# ---------------------------------------------------------
# [DTO 모델] Java와 필드명이 100% 일치해야 함
# ---------------------------------------------------------
class UserProfile(BaseModel):
    height: float
    weight: float
    age: int
    gender: str
    activity: str
    goal: str
    # Java에서 보내는 필드명(camelCase) 그대로 받기
    healthCondition: str | None = "없음"
    allergies: str | None = "없음"
    dislikedFood: str | None = "없음"
    targetWeight: float | None = None


class DailyLog(BaseModel):
    date: str
    dietSummary: str


class DietAnalysisRequest(BaseModel):
    userProfile: UserProfile
    dailyLog: DailyLog


# ---------------------------------------------------------
# AI 분석 엔드포인트
# ---------------------------------------------------------
@app.post("/analyze")
def analyze_diet(request: DietAnalysisRequest):
    print(f"📩 [Java 요청 도착] {request}")

    try:
        profile = request.userProfile
        log = request.dailyLog

        target_msg = f"{profile.targetWeight}kg" if profile.targetWeight else "설정 안함"

        # -----------------------------------------------------
        # [핵심] 시스템 프롬프트: RPG 컨셉 주입
        # -----------------------------------------------------
        system_instruction = f"""
        너는 판타지 게임의 '게임 마스터(GM)'이자 영양 전문가야.
        사용자의 식단을 분석해서 게임 스탯과 퀘스트로 변환해줘.

        [사용자 프로필]
        - 상태: {profile.healthCondition} (관련된 디버프 주의)
        - 금지 아이템(알레르기): {profile.allergies} (절대 추천 금지)
        - 기피 아이템: {profile.dislikedFood}
        - 목표: {profile.goal} (목표 달성 시 경험치 획득)

        [RPG 세계관 규칙]
        1. 단백질 -> STR (근력/공격력)
        2. 탄수화물 -> EGY (에너지/활동력)
        3. 지방 -> DEF (방어력/내구도)
        4. 나트륨/당/술 -> TOXIC (독성/경고)
        5. 비타민/수분/식이섬유 -> POTION (포션/회복)
        6. 식단 밸런스 -> BALANCE (균형)

        [응답 포맷 (JSON Only)]
        반드시 아래 JSON 형식을 지켜. (마크다운 ```json 없이)

        {{
          "score": 0~100 정수 (오늘의 전투력),
          "rank": "S/A/B/C/F 중 하나 (점수 기반 등급)",
          "dailyTitle": "오늘의 칭호 (예: 근육 몬스터, 설탕 슬라임, 클린 용사 등 창의적으로)",
          "oneLineSummary": "GM의 총평 (게임 용어 사용, 반말 컨셉)",

          "insights": [
            {{
              "type": "positive", 
              "iconType": "sword", 
              "title": "STR 대폭 상승!", 
              "description": "단백질 섭취가 훌륭하다. 공격력이 증가했다."
            }},
            {{
              "type": "warning", 
              "iconType": "skull", 
              "title": "TOXIC 수치 위험", 
              "description": "나트륨이 너무 높다. 해독 포션(물)을 마셔라."
            }},
            {{
              "type": "suggestion", 
              "iconType": "scroll", 
              "title": "NEXT QUEST", 
              "description": "채소 50g을 파밍해라. 방어력이 오를 것이다."
            }}
          ],

          "recommendations": [
            {{ "menu": "내일 아침 추천 메뉴", "reason": "퀘스트 목표 (예: STR 보충)" }},
            {{ "menu": "내일 점심 추천 메뉴", "reason": "퀘스트 목표" }},
            {{ "menu": "내일 저녁 추천 메뉴", "reason": "퀘스트 목표" }}
          ]
        }}
        """

        user_message = f"""
        [오늘의 모험 기록(식단)]
        날짜: {log.date}
        내용: {log.dietSummary}
        """

        # -----------------------------------------------------
        # AI 호출
        # -----------------------------------------------------
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": user_message}
            ],
            temperature=0.5  # RPG 컨셉이라 창의성 약간 높임
        )

        response_content = completion.choices[0].message.content
        print(f"🤖 [AI 원본 응답] {response_content}")

        # 마크다운 제거
        if response_content.startswith("```json"):
            response_content = response_content.replace("```json", "").replace("```", "")
        elif response_content.startswith("```"):
            response_content = response_content.replace("```", "")

        return json.loads(response_content)

    except json.JSONDecodeError as e:
        print(f"❌ [JSON 파싱 에러] {e}")
        # 에러 시 기본값 반환
        return {
            "score": 0,
            "rank": "F",
            "dailyTitle": "시스템 오류",
            "oneLineSummary": "AI 통신 중 오류가 발생했습니다.",
            "insights": [],
            "recommendations": []
        }
    except Exception as e:
        print(f"❌ [서버 내부 에러] {e}")
        raise HTTPException(status_code=500, detail=str(e))