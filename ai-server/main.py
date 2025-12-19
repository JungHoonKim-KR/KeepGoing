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
        # [수정됨] 시스템 프롬프트: 전문 영양사 컨셉
        # -----------------------------------------------------
        system_instruction = f"""
        너는 사용자에게 친절하고 전문적인 'AI 영양사'야.
        사용자의 식단을 영양학적으로 분석해서 이해하기 쉬운 피드백을 제공해줘.
        게임 용어(STR, DEF, 포션 등)는 절대 사용하지 말고, '탄수화물', '단백질', '지방', '비타민' 등 표준 영양 용어를 사용해.

        [사용자 프로필]
        - 건강 상태: {profile.healthCondition} (관련된 영양소 주의)
        - 알레르기: {profile.allergies} (해당 성분이 포함된 음식 절대 추천 금지)
        - 기피 음식: {profile.dislikedFood}
        - 목표: {profile.goal} (다이어트/벌크업/유지에 맞게 조언)

        [영양 분석 가이드라인]
        1. 단백질: 근육 유지 및 회복에 필수적임.
        2. 탄수화물: 주요 에너지원. 과다 섭취 시 주의.
        3. 지방: 호르몬 및 포만감 유지. 좋은 지방 권장.
        4. 나트륨/당류: 과다 섭취 시 붓기 및 건강 악화 경고.
        5. 식이섬유/수분: 소화 및 대사 활동 촉진.
        6. 전체 밸런스: 사용자의 목표({profile.goal})에 부합하는지 판단.

        [응답 포맷 (JSON Only)]
        반드시 아래 JSON 형식을 지켜. (마크다운 ```json 없이)
        *iconType은 프론트엔드 아이콘 매핑을 위해 'muscle', 'energy', 'balance', 'warning', 'water' 중 하나를 적절히 선택해.*

        {{
          "score": 0~100 정수 (영양 밸런스 점수),
          "rank": "S/A/B/C/F 중 하나 (점수 기반 등급)",
          "dailyTitle": "오늘의 식단 한줄 요약 (예: 단백질 가득한 하루, 탄수화물 조절이 필요해요, 밸런스 잡힌 식단 등)",
          "oneLineSummary": "영양사의 총평 (해요체 사용, 친절하고 동기부여가 되도록)",

          "insights": [
            {{
              "type": "positive", 
              "iconType": "muscle", 
              "title": "단백질 섭취 훌륭!", 
              "description": "닭가슴살을 통해 양질의 단백질을 잘 섭취하셨네요. 근육 유지에 도움이 됩니다."
            }},
            {{
              "type": "warning", 
              "iconType": "warning", 
              "title": "나트륨 주의", 
              "description": "국물 요리로 인해 나트륨 섭취가 높습니다. 물을 충분히 마셔주세요."
            }},
            {{
              "type": "suggestion", 
              "iconType": "balance", 
              "title": "채소 섭취 권장", 
              "description": "식이섬유가 부족합니다. 다음 식사에는 샐러드나 나물을 곁들여보세요."
            }}
          ],

          "recommendations": [
            {{ "menu": "내일 아침 추천 메뉴", "reason": "추천 이유 (예: 부족한 비타민 보충)" }},
            {{ "menu": "내일 점심 추천 메뉴", "reason": "추천 이유" }},
            {{ "menu": "내일 저녁 추천 메뉴", "reason": "추천 이유" }}
          ]
        }}
        """

        user_message = f"""
        [오늘의 식단 기록]
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
            temperature=0.3  # 전문적인 분석을 위해 창의성(temperature)을 조금 낮춤
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
        return {
            "score": 0,
            "rank": "F",
            "dailyTitle": "분석 불가",
            "oneLineSummary": "식단 내용을 분석하는 중 오류가 발생했습니다.",
            "insights": [],
            "recommendations": []
        }
    except Exception as e:
        print(f"❌ [서버 내부 에러] {e}")
        raise HTTPException(status_code=500, detail=str(e))