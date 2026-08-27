FROM python:3.12-slim
WORKDIR /app
COPY ml_service/requirements.txt /app/ml_service/requirements.txt
RUN pip install --no-cache-dir -r /app/ml_service/requirements.txt
COPY ml_service /app/ml_service
EXPOSE 8001
CMD ["python", "-m", "uvicorn", "ml_service.main:app", "--host", "0.0.0.0", "--port", "8001"]
