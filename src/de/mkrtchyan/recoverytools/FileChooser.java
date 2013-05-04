package de.mkrtchyan.recoverytools;

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileChooser extends Dialog {

	private static File Start;
	private static File currentPath;
	public boolean use = false;
	public File selectedFile;
	TextView tvPath;
	ListView lvFiles;
	Context context;
	String[] files;
	int select;
	Runnable runAtChoose;
	
	public FileChooser(final Context context, String StartPath, Runnable runAtChoose) {
		super(context);
		
		Start = new File(StartPath);
		currentPath = Start;
		this.context = context;
		this.runAtChoose = runAtChoose;
		setContentView(R.layout.dialog_file_chooser);
		setTitle(R.string.file_chooser);
		
		tvPath = (TextView) findViewById(R.id.tvPath);
		lvFiles = (ListView) findViewById(R.id.lvFiles);
		reload();
		
		lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				select = arg2;
				selectedFile = new File(currentPath, files[select]);
				if (selectedFile.isDirectory()){
					if (selectedFile.list().length > 0) {
						currentPath = selectedFile;
						reload();
					} else {
						Toast.makeText(context, String.format(context.getString(R.string.empty_dir), selectedFile.getAbsolutePath()), Toast.LENGTH_SHORT).show();
						currentPath = Start;
						reload();
					}
				} else {
					fileSelected();
				}
			}
		});
		show();
		
	}
	
	public void reload() {
		tvPath.setText(currentPath.getAbsolutePath());
		files = currentPath.list();
		
		lvFiles.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, files));
	}
	
	public void fileSelected() {
		AlertDialog.Builder abuilder = new AlertDialog.Builder(context);
		abuilder
			.setTitle(R.string.warning)
			.setMessage(String.format(context.getString(R.string.choose_message), selectedFile.getName()))
			.setPositiveButton(R.string.positive, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					use = true;
					runAtChoose.run();
					use = false;
					dismiss();
				}
			})
			.setNegativeButton(R.string.negative, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					use = false;
					currentPath = Start;
					reload();
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					use = false;
				}
			})
			.show();
	}
	
	public Dialog getDialog() {
		return this;
	}
}
