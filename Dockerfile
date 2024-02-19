FROM openjdk:23-oracle

WORKDIR /app

COPY build/libs/*.jar /app/LyceumTeacherBot.jar

EXPOSE 8443

ENTRYPOINT ["java","-jar","LyceumTeacherBot.jar", "--spring.main.banner-mode=off", "--bot.name=lyceumteacher_bot", "--bot.token=6587087875:AAEPvLlrEJqlR7iaBcyLt74iH32iHT8W1EE", "--url.firstPart=https://sheets.googleapis.com/v4/spreadsheets/", "--url.sheetId=18rB2icINi76QRReTq8LHOunTDeJ7rCozgIBe9WQ59jE", "--url.apiKey=AIzaSyAzk-drrOTIcX5bFIUSwMHFVWt_ftucJeE", "--baseIdList=baseIdList"]