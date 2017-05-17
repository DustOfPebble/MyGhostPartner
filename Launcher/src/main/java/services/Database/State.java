package services.Database;

class State {
    static final int Waiting = 1; // DB has to be cleared an Wait until task finished !
    static final int Loading = 2; // DB is Loading data from files and accepting GPS update
    static final int Idle = 3; // DB is only accepting GPS update
}
