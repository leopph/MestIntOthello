///leopph,Loffler.Levente@stud.u-szeged.hu
import java.util.Random;

import game.engine.utils.Utils;
import game.oth.OthelloAction;
import game.oth.OthelloGame;
import game.oth.OthelloPlayer;


/**
 * Egy Alfa-Beta Min-Max algoritmust megvalosíto jatekos.
 */
public class Agent extends OthelloPlayer
{
    /**
     * A felepitett keresesi fa maximalis melysege.
     */
    private static final int SEARCH_DEPTH = 5;
    /**
     * Az ellenfel szine. Csak cache celzattal.
     */
    private final int m_OpponentColor;


    /**
     * Adatszerkezet a lepesek es az ezekbol keletkezo jatektablak osszecsatolasara.
     */
    private static class BoardState
    {
        /**
         * A lepes, ami ezt az allast eredmenyezte.
         */
        public final OthelloAction Action;
        /**
         * A tabla aktualis allapota.
         */
        public final int[][] Board;


        public BoardState(int[][] board, final OthelloAction action)
        {
            Action = action;
            Board = board;
        }


        /**
         * A tabla heurisztikus erteke.
         * @param playerColor az MI szine a tablan
         * @return A tablan talalhato kulonbozo figurak kozotti darabszam elterest. Pozitiv, ha az Agentnek van tobb.
         */
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

        // A legjobb lepes minimax erteke
        long bestValue = 0;
        // A legjobb lepes
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
                    // Az adott lepes minimax erteke
                    final var value = MinAlphaBeta(new BoardState(newBoard, action), SEARCH_DEPTH, Long.MIN_VALUE, Long.MAX_VALUE);
                    // Ha ez jobb, mint az eddig ismert, csere
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


    /**
     * A MiniMax algoritmus maximalizalo aga.
     * @param currentBoard a node, aminek a gyerekein a maximalizalast vegezzuk
     * @param searchDepth az aktualis keresesi melyseg
     * @param alpha az aktualis alpha ertek (nyereseg)
     * @param beta az aktualis beta ertek (veszteseg)
     * @return a tabla gyerekeinek MiniMax ertekenek maximumat.
     */
    private long MaxAlphaBeta(final BoardState currentBoard, int searchDepth, long alpha, final long beta)
    {
        /* Ha a elertuk a maximalis keresesi erteket, vagy esetleg tele a tabla,
         * akkor a node egy level es visszaadjuk a tablajanak heurisztikus erteket. */
        if (searchDepth == 0 || Full(currentBoard.Board))
        {
            return currentBoard.HeuristicValue(color);
        }

        // A legrosszabb, szurrealis eset
        var ret = Long.MIN_VALUE;

        for (int i = 0; i < currentBoard.Board.length; i++)
        {
            for (int j = 0; j < currentBoard.Board[i].length; j++)
            {
                if (OthelloGame.isValid(currentBoard.Board, i, j, color))
                {
                    var child = new BoardState(Utils.copy(currentBoard.Board), new OthelloAction(i, j));
                    OthelloGame.setAction(child.Board, i, j, color);
                    // Jobb legrosszabb esetet keresunk
                    ret = Math.max(ret, MinAlphaBeta(child, searchDepth - 1, alpha, beta));
                    // Tobb a garantalt nyereseg, mint a veszteseg, 'vaghatunk'
                    if (ret >= beta)
                    {
                        return ret;
                    }
                    // Az eddiginel jobb megoldast keressuk innentol
                    alpha = Math.max(alpha, ret);
                }
            }
        }

        /* Ha nem volt valid lepes (azaz maradt a legrosszabb eset), akkor
         * egy levelet vizsgaltunk, ahol a tabla nincs tele, de mar nem lehet sehova lepni,
         * igy a jateknak vege. Ekkor, mivel ez egy level, visszaterunk a tabla heurisztikus ertekevel. */
        return ret == Long.MIN_VALUE ? currentBoard.HeuristicValue(color) : ret;
    }


    /**
     * A MiniMax algoritmus minimalizalo aga.
     * @param currentBoard a node, aminek a gyerekein a minimalizalast vegezzuk
     * @param searchDepth az aktualis keresesi melyseg
     * @param alpha az aktualis alpha ertek (nyereseg)
     * @param beta az aktualis beta ertek (veszteseg)
     * @return a tabla gyerekeinek MiniMax ertekenek minimuma.
     */
    private long MinAlphaBeta(final BoardState currentBoard, int searchDepth, final long alpha, long beta)
    {
        /* Ha a elertuk a maximalis keresesi erteket, vagy esetleg tele a tabla,
         * akkor a node egy level es visszaadjuk a tablajanak heurisztikus erteket. */
        if (searchDepth == 0 || Full(currentBoard.Board))
        {
            return currentBoard.HeuristicValue(color);
        }

        // A legjobb, szurrealis eset
        var ret = Long.MAX_VALUE;

        for (int i = 0; i < currentBoard.Board.length; i++)
        {
            for (int j = 0; j < currentBoard.Board[i].length; j++)
            {
                if (OthelloGame.isValid(currentBoard.Board, i, j, m_OpponentColor))
                {
                    var child = new BoardState(Utils.copy(currentBoard.Board), new OthelloAction(i, j));
                    OthelloGame.setAction(child.Board, i, j, m_OpponentColor);
                    // Rosszabb legjobb esetet keresunk
                    ret = Math.min(ret, MaxAlphaBeta(child, searchDepth - 1, alpha, beta));
                    // Kevesebb a garantalt veszteseg, mint a nyereseg, 'vaghatunk'
                    if (ret <= alpha)
                    {
                        return ret;
                    }
                    beta = Math.min(beta, ret);
                }
            }
        }

        /* Ha nem volt valid lepes (azaz maradt a legrosszabb eset), akkor
         * egy levelet vizsgaltunk, ahol a tabla nincs tele, de mar nem lehet sehova lepni,
         * igy a jateknak vege. Ekkor, mivel ez egy level, visszaterunk a tabla heurisztikus ertekevel. */
        return ret == Long.MAX_VALUE ? currentBoard.HeuristicValue(color) : ret;
    }


    /**
     * @param board a vizsgalando tabla
     * @return tele-e van-e (azaz nincs tobb ures mezo)
     */
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
