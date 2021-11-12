@echo off
xcopy /q /y out\production\MestIntOthello\Agent.class .
xcopy /q /y out\production\MestIntOthello\Agent$BoardState.class .
xcopy /q /y lib\game_engine.jar .
if exist Agent.class (
    if exist game_engine.jar (
        java -jar game_engine.jar 10 game.oth.OthelloGame 1234567890 10 3 2000 game.oth.players.GreedyPlayer Agent
        del game_engine.jar
    )
    del Agent.class
    del Agent$BoardState.class
)