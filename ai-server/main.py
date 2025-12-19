from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from openai import OpenAI
import os
from dotenv import load_dotenv
import json

load_dotenv()
API_KEY = os.getenv("OPENAI_API_KEY")

client = OpenAI(
    api_key=API_KEY,
    base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
)

app = FastAPI()


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


@app.post("/analyze")
def analyze_diet(request: DietAnalysisRequest):
    print(f"📩 [요청] {request.dailyLog.date}")

    try:
        system_instruction = f"""
        너는 'AI 영양사'야.
        1. 식단을 영양학적으로 분석해줘.
        2. 식단에서 '아이템'으로 만들만한 대표 음식 키워드 5개를 명사 형태로 추출해줘.

        [응답 포맷 (JSON Only)]
        {{
          "score": 0~100 정수,
          "rank": "S/A/B/C/F",
          "dailyTitle": "한줄 타이틀",
          "oneLineSummary": "한줄 총평",
          "insights": [
             {{ "type": "positive", "iconType": "muscle", "title": "제목", "description": "내용" }}
          ],
          "recommendations": [
             {{ "menu": "추천메뉴", "reason": "이유" }}
          ],
          "miningKeywords": [
             "닭가슴살", "현미밥", "고구마", "아메리카노", "사과"
          ]
        }}
        """

        user_message = f"식단: {request.dailyLog.dietSummary}\n목표: {request.userProfile.goal}"

        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": system_instruction},
                {"role": "user", "content": user_message}
            ],
            temperature=0.3
        )

        response_content = completion.choices[0].message.content
        clean_content = response_content.replace("```json", "").replace("```", "").strip()

        return json.loads(clean_content)

    except Exception as e:
        print(f"❌ 에러: {e}")
        return {
            "score": 0, "rank": "F", "dailyTitle": "에러", "oneLineSummary": "실패",
            "insights": [], "recommendations": [], "miningKeywords": []
        }