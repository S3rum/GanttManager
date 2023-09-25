package backend;


import dom2app.SimpleTableModel;

import javax.swing.text.Document;
import java.io.*;
import java.util.*;



public class MainController implements IMainController {

	private SimpleTableModel simpleTableModel;

	private List<String[]> topLevelTasks = new ArrayList<>();
	private List<String[]> newData = new ArrayList<String[]>();
	private String[] columnNames = new String[]{"TaskId", "TaskText", "MamaId", "Start", "End", "Cost"};



	public boolean checkIfMama(String mamaId) {
		return Integer.parseInt(mamaId) == 0;
	}

	public boolean checkTaskPriority(String[] task1, String[] task2) {
		return Integer.parseInt(task1[3]) == Integer.parseInt(task2[3]) &&
				Integer.parseInt(task1[0]) > Integer.parseInt(task2[0]);
	}

	public List<String[]> sortData (List<String[]> data) {
		List<String[]> subtasks = new ArrayList<String[]>();
		List<String[]> mamas = new ArrayList<>();

		for (String[] line: data) {

			if (checkIfMama(line[2]))
				mamas.add(line);
			else
				subtasks.add(line);
		}

		subtasks.sort(Comparator.comparing(a -> Integer.parseInt(a[3])));

		int numSwaps = 0;
		while (true) {
			for (int i = 1; i < subtasks.size() - 1; i ++) {
				if (checkTaskPriority(subtasks.get(i-1), subtasks.get(i))) {
					Collections.swap(subtasks, i-1, i);
					numSwaps ++;
				}
			}
			if (numSwaps == 0) break;
			numSwaps = 0;
		}



		int currentMamaId = 0;
		boolean isFirstFlag = true;

		int firstIndex = 0;

		String[] currentSubtask = {};

		for (int i = 0; i < mamas.size(); i ++) {
			currentMamaId = Integer.parseInt(mamas.get(i)[0]);
			boolean passes = false;
			List<String> mamaAsList = new ArrayList<String>(); //.toArray(new String[0]);

			for (String el: mamas.get(i))
				mamaAsList.add(el);

			int mamaCost = 0;
			for (String[] subtask: subtasks) {

				currentSubtask = subtask;
				if (Integer.parseInt(currentSubtask[2]) == currentMamaId) {
					passes = true;
					if (isFirstFlag) {
						mamaAsList.add(currentSubtask[3]);

						isFirstFlag = false;
					}
					mamaCost += Integer.parseInt(currentSubtask[5]);
					newData.add(currentSubtask);

					firstIndex ++;
				}


			}

			mamaAsList.add(currentSubtask[4]);
			mamaAsList.add(String.valueOf(mamaCost));



			if (i != 0 && passes)
				newData.add(firstIndex-1 , mamaAsList.toArray(new String[0]));

			else if (i != 0 && !passes) {
				firstIndex ++;
				newData.add(firstIndex, mamas.get(i));

			}
			else if (i == 0)
				newData.add(0, mamaAsList.toArray(new String[0]));

			topLevelTasks.add(mamaAsList.toArray(new String[0]));

			isFirstFlag = true;


		}

		return newData;
	}


	@Override
	public SimpleTableModel load(String fileName, String delimiter) throws IOException{
		File file = new File(fileName);
		BufferedReader br = new BufferedReader(new FileReader(file));

		List<String[]> data = new ArrayList<String[]>();
		String line;
		while ((line = br.readLine()) != null) data.add(line.split("\t"));
		br.close();

		String prjName = fileName.substring(fileName.lastIndexOf('\\') + 1, fileName.lastIndexOf('.'));


		simpleTableModel = new SimpleTableModel(fileName, prjName, sortData(data));


		return simpleTableModel;
	}




	public boolean startsWithSubstring (String taskText, String prefix) {
		return taskText.startsWith(prefix);
	}


	public boolean isEqualToId (String taskId, int id) {
		return Integer.parseInt(taskId) == id;
	}

	public SimpleTableModel getTasksBy (String by, String prefixOrId) {
		List<String[]> newData = new ArrayList<String[]>();

		SimpleTableModel newSimpleTableModel = new SimpleTableModel(this.simpleTableModel.getName(), this.simpleTableModel.getPrjName(),
				this.simpleTableModel.getData());


		if (by.equalsIgnoreCase("prefix")) {
			for (String[] line: newSimpleTableModel.getData())
				if (startsWithSubstring(line[1], prefixOrId))
					newData.add(line);
		}

		else for (String[] line: newSimpleTableModel.getData())
			if (isEqualToId(line[0], Integer.parseInt(prefixOrId)))
				newData.add(line);

		newSimpleTableModel.setData(newData);


		return newSimpleTableModel;
	}


	@Override
	public SimpleTableModel getTasksByPrefix(String prefix) {
		return getTasksBy("prefix", prefix);
	}


	@Override
	public SimpleTableModel getTaskById(int id) {
		return getTasksBy("id", String.valueOf(id));
	}

	@Override
	public SimpleTableModel getTopLevelTasks() {
		SimpleTableModel newSimpleTableModel = new SimpleTableModel(this.simpleTableModel.getName(), this.simpleTableModel.getPrjName(),
				topLevelTasks);

		return newSimpleTableModel;
	}
	private void print(PrintWriter pr, String[] data, ReportType type){
		for(int i = 0;i< data.length;i++) {
			if (!(i == data.length - 1)) {
				pr.print(data[i] + "\t");
			} else {
				if(type == ReportType.TEXT){
					pr.print(data[i] + "\n");
				} else if(type == ReportType.MD){
					pr.print(data[i]);
				}
			}
		}
	}
	private void getText(String path){
		try{
			PrintWriter pr = new PrintWriter(path+".txt");
			print(pr,columnNames,ReportType.TEXT);
			for(String[] data:newData){
				print(pr,data,ReportType.TEXT);
			}
			pr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void getMD(String path){
		try{
			PrintWriter pr = new PrintWriter(path+".md");
			pr.print("_");
			print(pr,columnNames,ReportType.MD);
			pr.print("_");
			for(String[] data:newData){
				if (Integer.parseInt(data[2]) == 0){
					pr.println("**");
					print(pr,data,ReportType.MD);
					pr.print("**");
				} else {
					print(pr,data,ReportType.MD);
				}
			}
			pr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	private void getHTML(String path){
		try {
			PrintWriter pr = new PrintWriter(path+".html");
			pr.println("<html>");
			pr.println("<head>");
			pr.println("<title>Gantt Project Data</title>");
			pr.println("</head>");
			pr.println("<body>");
			pr.println("<table>");
			pr.println("<tr>");
			for(String colum:columnNames){
				pr.print("<td>"+colum+"</td>");
			}
			pr.print("</tr>");
			for(String[] data:newData){
				pr.println("<tr>");
				for(String string:data){
					pr.print("<td>"+string+"</td>");
				}
				pr.print("</tr>");
			}
			pr.println("</table>");
			pr.println("</html>");
			pr.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	@Override
	public int createReport(String path, ReportType type) {
		if(type == ReportType.TEXT){
			getText(path);
		} else if (type == ReportType.MD){
			getMD(path);
		} else{
			getHTML(path);
		}
		return 0;
	}
	public static void main(String[] args) throws IOException {
		MainController controller = new MainController();
		controller.load("D:\\Uni\\Software Development\\revised_2022-23_AM1_AM2_AM3_GanttManager_Public\\revised_2022-23_AM1_AM2_AM3_GanttManager_Public\\src\\main\\resources\\input\\EggsScrambled.tsv","    ");
	}
}


