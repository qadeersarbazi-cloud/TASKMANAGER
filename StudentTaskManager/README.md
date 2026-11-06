# TaskFlow - Student Task Manager with Smart Priority Queue (Java)

Dark-themed Swing GUI on top of the DSA project from the proposal. No external libraries, no database.

## Data structures / algorithms (same as the proposal)
- Stage 1: ArrayList with add / update / delete / display
- Stage 2: Linear search (name, ID) and Bubble / Insertion / Merge Sort by deadline (hand-written)
- Stage 3: HashMap (ID -> assignment) for O(1) average search by ID
- Stage 4: PriorityQueue (two heaps) for "most urgent" and "top priority"
- Stage 5: Big-O benchmark with charts (sidebar button)

Extra: assignments are saved automatically to `assignments.txt` (plain text) and loaded on start.

## Run the GUI (JDK 17+)
    mkdir out
    javac -d out src/project/*.java          (Windows: javac -d out src\project\*.java)
    java -cp out project.TaskManagerGUI

VS Code: open this folder, open `src/project/TaskManagerGUI.java`, press Run.

## Optional console version
    java -cp out project.Main

## Using the GUI
- Search box + dropdown: Name (linear), ID (linear) or ID (HashMap). Footer shows how long the lookup took.
- Order dropdown: As added, Bubble, Insertion or Merge Sort by deadline (footer shows the sort time).
- Stat cards "Most urgent" and "Top priority" are read straight from the heaps (O(1) peek).
- Double-click a row to edit, or select it and use Edit / Delete (Delete key also works).
- "Under the hood" panel shows the size of each data structure.

## Files
Assignment, TaskManager (ArrayList + HashMap + heaps), SortAlgorithms, FileStorage, Benchmark,
Main (console), TaskManagerGUI / AppPanel / FormPanel / BenchmarkPanel / Ui (graphical interface).
