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
        public final BoardState Parent;

        BoardState(final OthelloAction action, int[][] board, BoardState parent)
        {
            Action = action;
            Board = board;
            Parent = parent;
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
        var currentBoardState = new BoardState(prevAction, board, null);
        var preferredAction = Max(currentBoardState, 0).Action;
        OthelloGame.setAction(board, preferredAction.i, preferredAction.j, color);
        return preferredAction;
    }


    private BoardState Max(final BoardState currentBoard, int searchDepth)
    {
        if (searchDepth == SEARCH_DEPTH || Full(currentBoard.Board))
        {
            return currentBoard;
        }

        BoardState ret = null;

        for (int i = 0; i < currentBoard.Board.length; i++)
        {
            for (int j = 0; j < currentBoard.Board[i].length; j++)
            {
                if (OthelloGame.isValid(currentBoard.Board, i, j, color))
                {
                    var childBoard = Utils.copy(currentBoard.Board);
                    OthelloGame.setAction(childBoard, i, j, color);
                    var child = new BoardState(new OthelloAction(i, j), childBoard, currentBoard);
                    var childMin = Min(child, searchDepth + 1);
                    if (ret == null || Value(ret.Board, color) < Value(childMin.Board, color))
                    {
                        ret = childMin;
                    }
                }
            }
        }
        return ret;
    }


    private BoardState Min(final BoardState currentBoard, int searchDepth)
    {
        if (searchDepth == SEARCH_DEPTH || Full(currentBoard.Board))
        {
            return currentBoard;
        }

        BoardState ret = null;

        for (int i = 0; i < currentBoard.Board.length; i++)
        {
            for (int j = 0; j < currentBoard.Board[i].length; j++)
            {
                if (OthelloGame.isValid(currentBoard.Board, i, j, m_OpponentColor))
                {
                    var childBoard = Utils.copy(currentBoard.Board);
                    OthelloGame.setAction(childBoard, i, j, m_OpponentColor);
                    var child = new BoardState(new OthelloAction(i, j), childBoard, currentBoard);
                    var childMax = Max(child, searchDepth + 1);
                    if (ret == null || Value(ret.Board, m_OpponentColor) > Value(childMax.Board, m_OpponentColor))
                    {
                        ret = childMax;
                    }
                }
            }
        }
        return ret;
    }


    private long Value(final int[][] board, final int evalColor)
    {
        final int otherColor = evalColor == color ? m_OpponentColor : color;
        long ret = 0;
        for (var row : board)
        {
            for (var tile : row)
            {
                if (tile == evalColor)
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
