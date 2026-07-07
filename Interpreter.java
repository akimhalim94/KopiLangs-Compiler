import kopilang.node.*;
import java.util.*;

public class Interpreter {

    private final Map<String, Double> values = new LinkedHashMap<>();
    private final Map<String, String> types = new LinkedHashMap<>();
    private final boolean trace;

    public Interpreter(boolean trace) {
        this.trace = trace;
    }

    // RUN
    public void run(Start ast) {

        AProgram p = (AProgram) ast.getPProgram();

        log("--- Program \"" + p.getClassName().getText() + "\" starting ---");

        execStatements(p.getStatements());

        log("--- Program finished ---");
    }

    private void log(String s) {
        if (trace)
            System.out.println(s);
    }

    // STATEMENTS
    private void execStatements(PStatements ps) {

        if (ps == null)
            return;

        for (Object o : ((AStatements) ps).getStatement()) {
            execStatement((PStatement) o);
        }
    }

    private void execStatement(PStatement s) {

        if (s instanceof AMatchedStatement) {
            execMatched(((AMatchedStatement) s).getMatchedStmt());
        } else {
            execUnmatched(((AUnmatchedStatement) s).getUnmatchedStmt());
        }
    }

    // MATCHED STATEMENTS

    private void execMatched(PMatchedStmt m) {

        if (m instanceof ADeclarationMatchedStmt) {

            ADeclarationMatchedStmt d = (ADeclarationMatchedStmt) m;

            String type = (d.getVarType() instanceof AIntVarType) ? "int" : "float";

            for (String id : idList(d.getIdList())) {
                values.put(id, 0.0);
                types.put(id, type);
                log("declare " + type + " " + id);
            }

        } else if (m instanceof AAssignmentMatchedStmt) {

            AAssignmentMatchedStmt a = (AAssignmentMatchedStmt) m;

            String id = a.getId().getText();
            double v = evalExpr(a.getExpr());

            values.put(id, v);
            log("Initialise " + id + " = " + fmt(v));

        } else if (m instanceof ABlockMatchedStmt) {

            execStatements(((ABlockMatchedStmt) m).getStatements());

        }

        // FOR LOOP

        else if (m instanceof AForMatchedStmt) {

            AForMatchedStmt f = (AForMatchedStmt) m;

            String id = f.getInit().getText();
            values.put(id, evalExpr(f.getInitExpr()));

            int guard = 0;

            while (evalExpr(f.getCond()) != 0.0) {

                execMatched(f.getMatchedStmt());

                values.put(
                        f.getStep().getText(),
                        evalExpr(f.getStepExpr()));

                if (++guard > 1_000_000)
                    throw new RuntimeException("Infinite for-loop");
            }

        }

        // WHILE

        else if (m instanceof AWhileMatchedStmt) {

            AWhileMatchedStmt w = (AWhileMatchedStmt) m;

            int guard = 0;

            while (evalExpr(w.getExpr()) != 0.0) {
                execMatched(w.getMatchedStmt());
                if (++guard > 1_000_000)
                    throw new RuntimeException("Infinite loop");
            }

        }

        // IF-ELSE

        else if (m instanceof AIfelseMatchedStmt) {

            AIfelseMatchedStmt i = (AIfelseMatchedStmt) m;

            if (evalExpr(i.getExpr()) != 0.0)
                execMatched(i.getIfStmt());
            else
                execMatched(i.getElseStmt());

        }

        // DO-WHILE
        else if (m instanceof ADowhileMatchedStmt) {

            ADowhileMatchedStmt d = (ADowhileMatchedStmt) m;

            int guard = 0;

            do {
                execStatement(d.getStatement());
                if (++guard > 1_000_000)
                    throw new RuntimeException("Infinite do-while loop");
            } while (evalExpr(d.getExpr()) != 0.0);

        } else {
            throw new RuntimeException("Unknown matched stmt: " + m.getClass());
        }
    }

    // UNMATCHED STATEMENTS

    private void execUnmatched(PUnmatchedStmt u) {

        if (u instanceof AIfUnmatchedStmt) {

            AIfUnmatchedStmt i = (AIfUnmatchedStmt) u;

            if (evalExpr(i.getExpr()) != 0.0)
                execStatement(i.getStatement());

        } else if (u instanceof AIfelseUnmatchedStmt) {

            AIfelseUnmatchedStmt i = (AIfelseUnmatchedStmt) u;

            if (evalExpr(i.getExpr()) != 0.0)
                execMatched(i.getIfStmt());
            else
                execUnmatched(i.getElseStmt());

        } else if (u instanceof AWhileUnmatchedStmt) {

            AWhileUnmatchedStmt w = (AWhileUnmatchedStmt) u;

            int guard = 0;

            while (evalExpr(w.getExpr()) != 0.0) {
                execUnmatched(w.getUnmatchedStmt());
                if (++guard > 1_000_000)
                    throw new RuntimeException("Infinite loop");
            }

        } else {
            throw new RuntimeException("Unknown unmatched stmt: " + u.getClass());
        }
    }

    // EXPRESSIONS

    private double evalExpr(PExpr e) {

        if (e instanceof ALtExpr) {
            ALtExpr x = (ALtExpr) e;
            return evalExpr(x.getExpr()) < evalLogicTerm(x.getLogicTerm()) ? 1.0 : 0.0;

        } else if (e instanceof ALteqExpr) {
            ALteqExpr x = (ALteqExpr) e;
            return evalExpr(x.getExpr()) <= evalLogicTerm(x.getLogicTerm()) ? 1.0 : 0.0;

        } else if (e instanceof AGtExpr) {
            AGtExpr x = (AGtExpr) e;
            return evalExpr(x.getExpr()) > evalLogicTerm(x.getLogicTerm()) ? 1.0 : 0.0;

        } else if (e instanceof AGteqExpr) {
            AGteqExpr x = (AGteqExpr) e;
            return evalExpr(x.getExpr()) >= evalLogicTerm(x.getLogicTerm()) ? 1.0 : 0.0;

        } else if (e instanceof AEqExpr) {
            AEqExpr x = (AEqExpr) e;
            return evalExpr(x.getExpr()) == evalLogicTerm(x.getLogicTerm()) ? 1.0 : 0.0;

        } else if (e instanceof ALogicTermExpr) {
            return evalLogicTerm(((ALogicTermExpr) e).getLogicTerm());
        }

        throw new RuntimeException("Unknown expr: " + e.getClass());
    }

    // + / -

    private double evalLogicTerm(PLogicTerm t) {

        if (t instanceof APlusLogicTerm) {
            APlusLogicTerm x = (APlusLogicTerm) t;
            return evalLogicTerm(x.getLogicTerm()) + evalTerm(x.getTerm());

        } else if (t instanceof AMinusLogicTerm) {
            AMinusLogicTerm x = (AMinusLogicTerm) t;
            return evalLogicTerm(x.getLogicTerm()) - evalTerm(x.getTerm());

        } else if (t instanceof ATermLogicTerm) {
            return evalTerm(((ATermLogicTerm) t).getTerm());
        }

        throw new RuntimeException("Unknown logic term: " + t.getClass());
    }

    // * /

    private double evalTerm(PTerm t) {

        if (t instanceof AMultTerm) {
            AMultTerm x = (AMultTerm) t;
            return evalTerm(x.getTerm()) * evalFactor(x.getFactor());

        } else if (t instanceof ADivTerm) {
            ADivTerm x = (ADivTerm) t;
            return evalTerm(x.getTerm()) / evalFactor(x.getFactor());

        } else if (t instanceof AFactorTerm) {
            return evalFactor(((AFactorTerm) t).getFactor());
        }

        throw new RuntimeException("Unknown term: " + t.getClass());
    }

    // FACTORS
    private double evalFactor(PFactor f) {

        if (f instanceof AIdFactor) {
            String id = ((AIdFactor) f).getId().getText();
            return values.getOrDefault(id, 0.0);

        } else if (f instanceof AIntFactor) {
            return Double.parseDouble(((AIntFactor) f).getIntVal().getText());

        } else if (f instanceof AFloatFactor) {
            return Double.parseDouble(((AFloatFactor) f).getFloatVal().getText());

        } else if (f instanceof ANegIntFactor) {
            return -Double.parseDouble(((ANegIntFactor) f).getIntVal().getText());

        } else if (f instanceof AParensFactor) {
            return evalExpr(((AParensFactor) f).getExpr());
        }

        throw new RuntimeException("Unknown factor: " + f.getClass());
    }

    // UTIL
    private List<String> idList(PIdList list) {

        List<String> names = new ArrayList<>();
        PIdList cur = list;

        while (cur != null) {

            if (cur instanceof ASingleIdList) {
                names.add(((ASingleIdList) cur).getId().getText());
                break;
            } else {
                AMultiIdList m = (AMultiIdList) cur;
                names.add(m.getId().getText());
                cur = m.getIdList();
            }
        }

        return names;
    }

    private String fmt(double v) {
        return (v == Math.floor(v)) ? String.valueOf((long) v) : String.valueOf(v);
    }

    // FINAL STATE OUTPUT
    public String finalStateReport() {

        StringBuilder sb = new StringBuilder();

        for (String id : types.keySet()) {

            Double v = values.get(id);
            if (v == null)
                v = 0.0;

            String type = types.get(id);

            String shown = type.equals("float")
                    ? String.format("%.1f", v)
                    : String.valueOf(v.longValue());

            sb.append(" ")
                    .append(id)
                    .append(" (")
                    .append(type)
                    .append(") = ")
                    .append(shown)
                    .append("\n");
        }

        return sb.toString();
    }
}