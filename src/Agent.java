///leopph,Loffler.Levente@stud.u-szeged.hu
import java.util.Random;

import game.engine.utils.Utils;
import game.oth.OthelloAction;
import game.oth.OthelloGame;
import game.oth.OthelloPlayer;


public class Agent extends OthelloPlayer
{
    private static final int SEARCH_DEPTH = 4;
    private final int m_OpponentColor;


    private static class BoardState
    {
        public final OthelloAction Action;
        public final int[][] Board;


        public BoardState(int[][] board, final OthelloAction action)
        {
            Action = action;
            Board = board;
        }


        public long HeuristicValue(final int playerColor)
        {
            final int otherColor = playerColor == OthelloGame.BLACK ? OthelloGame.WHITE : OthelloGame.BLACK;
            long ret = 0;
            for (var row : Board)
            {
                for (var tile : row)
                {
                    if (tile == playerColor)
                    {
                        ret++;
                    }
                    else if (tile == otherColor)
                    {
                        ret--;
                    }
                }
            }
            return ret;
        }
    }


    public Agent(int color, int[][] board, Random random)
    {
        super(color, board, random);
        m_OpponentColor = color == OthelloGame.BLACK ? OthelloGame.WHITE : OthelloGame.BLACK;
    }


    @Override
    public OthelloAction getAction(OthelloAction prevAction, long[] remainingTimes)
    {
        if (prevAction != null)
        {
            OthelloGame.setAction(board, prevAction.i, prevAction.j, color == OthelloGame.BLACK ? OthelloGame.WHITE : OthelloGame.BLACK);
        }

        long bestValue = 0;
        OthelloAction bestAction = null;

        for (int i = 0; i < board.length; i++)
        {
            for (int j = 0; j < board.length; j++)
            {
                if (OthelloGame.isValid(board, i, j , color))
                {
                    final var action = new OthelloAction(i, j);
                    final var newBoard = Utils.copy(board);
                    OthelloGame.setAction(newBoard, i, j, color);
                    final var value = Min(new BoardState(newBoard, action), 0);
                    if (bestAction == null || bestValue < value)
                    {
                        bestValue = value;
                        bestAction = new OthelloAction(i, j);
                    }
                }
            }
        }

        OthelloGame.setAction(board, bestAction.i, bestAction.j, color);
        return bestAction;
    }


    private long Max(final BoardState currentBoard, int searchDepth)
    {
        if (searchDepth == SEARCH_DEPTH || Full(currentBoard.Board))
        {
            return currentBoard.HeuristicValue(color);
        }

        Long ret = null;

        for (int i = 0; i < currentBoard.Board.length; i++)
        {
            for (int j = 0; j < currentBoard.Board[i].length; j++)
            {
                if (OthelloGame.isValid(currentBoard.Board, i, j, color))
                {
                    var child = new BoardState(Utils.copy(currentBoard.Board), new OthelloAction(i, j));
                    OthelloGame.setAction(child.Board, i, j, color);
                    var childMinValue = Min(child, searchDepth + 1);
                    if (ret == null || ret < childMinValue)
                    {
                        ret = childMinValue;
                    }
                }
            }
        }
        return ret == null ? currentBoard.HeuristicValue(color) : ret;
    }


    private long Min(final BoardState currentBoard, int searchDepth)
    {
        if (searchDepth == SEARCH_DEPTH || Full(currentBoard.Board))
        {
            return currentBoard.HeuristicValue(color);
        }

        Long ret = null;

        for (int i = 0; i < currentBoard.Board.length; i++)
        {
            for (int j = 0; j < currentBoard.Board[i].length; j++)
            {
                if (OthelloGame.isValid(currentBoard.Board, i, j, m_OpponentColor))
                {
                    var child = new BoardState(Utils.copy(currentBoard.Board), new OthelloAction(i, j));
                    OthelloGame.setAction(child.Board, i, j, m_OpponentColor);
                    var childMaxValue = Max(child, searchDepth + 1);
                    if (ret == null || ret > childMaxValue)
                    {
                        ret = childMaxValue;
                    }
                }
            }
        }
        return ret == null ? currentBoard.HeuristicValue(color) : ret;
    }


    private boolean Full(final int[][] board)
    {
        for (var row : board)
        {
            for (var tile : row)
            {
                if (tile == OthelloGame.EMPTY)
                {
                    return false;
                }
            }
        }
        return false;
    }
}
