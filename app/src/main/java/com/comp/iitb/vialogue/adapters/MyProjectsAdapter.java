package com.comp.iitb.vialogue.adapters;

/**
 * Created by jeffrey on 17/1/17.
 */

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.comp.iitb.vialogue.GlobalStuff.Master;
import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.coordinators.SharedRuntimeContent;
import com.comp.iitb.vialogue.library.Storage;
import com.comp.iitb.vialogue.models.ParseObjects.models.Project;
import com.comp.iitb.vialogue.models.ParseObjects.models.Slide;
import com.comp.iitb.vialogue.models.ParseObjects.models.interfaces.ParseObjectsCollection;
import com.comp.iitb.vialogue.models.ProjectsShowcase;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyProjectsAdapter extends RecyclerView.Adapter<MyProjectsAdapter.MyViewHolder> {

    private Context mContext;
    private Storage mStorage;
//    private List<ProjectsShowcase> mAlbumList;
    private int listItemPositionForPopupMenu;
    private ViewPager viewpager;
    private ArrayList<ProjectView> mProjectViewsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView thumbnail;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }

    public class ProjectView {

        private Project mProject;
        private Bitmap mThumbnail = null;

        public ProjectView(Project project) {
            mProject = project;
            generateThumbnail();
        }

        public String getProjectName() {
            return mProject.getName();
        }

        public Project getProject() {
            return mProject;
        }

        public Bitmap getThumbnail() {
            if(mThumbnail == null) {
                generateThumbnail();
            }
            return mThumbnail;
        }

        public Bitmap generateThumbnail() {
            mThumbnail = null;
            try {
                for(Slide s : mProject.getSlides().getAll()) {
                    if(s.getSlideType() == Slide.SlideType.IMAGE) {
                        // get thumbnail from image
                        mThumbnail = mStorage.getImageThumbnail(s.getResource().getResourceFile().getAbsolutePath());
                        break;
                    } else if(s.getSlideType() == Slide.SlideType.VIDEO) {
                        mThumbnail = mStorage.getVideoThumbnail(s.getResource().getResourceFile().getAbsolutePath());
                        break;
                    } else {}
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(mProject.getSlides().getAll());
            }

            return mThumbnail;
        }
    }

    public MyProjectsAdapter(Context context) {
        mContext = context;
        mStorage = new Storage(mContext);
        populateProjectsList();
    }

    public void populateProjectsList() {
        // populate mProjectViewsList
        mProjectViewsList = new ArrayList<ProjectView>();
        ArrayList<Project> localProjects = SharedRuntimeContent.getLocalProjects();
        for(Project project : localProjects) {
            mProjectViewsList.add(new ProjectView(project));
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_showcase_cardview, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override

    public void onBindViewHolder(final MyViewHolder holder, final int position) {
//        final ProjectsShowcase album = mAlbumList.get(position);
        final ProjectView projectView = mProjectViewsList.get(position);
        holder.title.setText(projectView.getProjectName());
//        holder.thumbnail.setImageBitmap(projectView.getThumbnail());

        Bitmap thumbnail = mProjectViewsList.get(position).getThumbnail();
        if(thumbnail == null) {
            thumbnail = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_logo);
        }
        holder.thumbnail.setImageBitmap(thumbnail);
//        Glide.with(mContext).load(f).placeholder(R.drawable.ic_computer_black_24dp).into(holder.thumbnail);


        holder.thumbnail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

//                // TODO change implementation API
                Project project = mProjectViewsList.get(position).getProject();
                SharedRuntimeContent.questionsList.clear();
                SharedRuntimeContent.project = SharedRuntimeContent.addThumbnailsToProject(mProjectViewsList.get(position).getProject(), mContext, mStorage);
                SharedRuntimeContent.updateAdapterView();
                SharedRuntimeContent.setProjectName(project.getName());
                viewpager=(ViewPager) ((Activity) mContext).findViewById(R.id.viewpager);
                viewpager.setCurrentItem(1,true);
            }
        });


       /* holder.overflow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(holder.overflow,holder.getAdapterPosition(),album.getName());
            }
        });*/


        holder.thumbnail.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                ((Activity) mContext).startActionMode(new ActionBarCallBack(holder.title.getText().toString(), holder.getAdapterPosition()));

                return false;
            }
        });

    }

    class ActionBarCallBack implements ActionMode.Callback {
        private String projectName;
        private int position;


        public ActionBarCallBack(String projectName, int position){
            this.projectName = projectName;
            this.position = position;

        }

        public boolean deleteProject(int position) {
            try {
                mProjectViewsList.get(position).getProject().unpin();
                mProjectViewsList.get(position).getProject().delete();
                mProjectViewsList.remove(position);
            } catch (com.parse.ParseException e) {
                Toast.makeText(mContext, R.string.wrongBuddy, Toast.LENGTH_SHORT);
                return false;
            } return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // delete project
            if(deleteProject(position)) {
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mProjectViewsList.size());
                mode.finish();
            } return false;

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            mode.getMenuInflater().inflate(R.menu.delete_projects, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {


        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub

            mode.setTitle("Confirm delete?");
            return false;
        }

    }




    private void showPopupMenu(View view, int listItemPosition, String projectName) {
        // inflate menu
        listItemPositionForPopupMenu = listItemPosition;
        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_card, popup.getMenu());
        popup.setOnMenuItemClickListener(new MyMenuItemClickListener(listItemPosition,projectName));
        popup.show();
    }

    /**
     * Click listener for options pop up menu items
     */
    class MyMenuItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private int listItemPosition;
        private String projectName;
        public MyMenuItemClickListener(int listItemPosition, String projectName) {
            this.listItemPosition = listItemPosition;
            this.projectName = projectName;

        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.deleteThis:
                    int newPosition = listItemPosition;
                    try {
                        mProjectViewsList.get(newPosition).getProject().unpin();
                        mProjectViewsList.get(newPosition).getProject().delete();
                        mProjectViewsList.remove(newPosition);
                        notifyItemRemoved(newPosition);
                        notifyItemRangeChanged(newPosition, mProjectViewsList.size());
                        return true;
                    } catch (Exception e) {
                        Toast.makeText(mContext, R.string.wrongBuddy, Toast.LENGTH_SHORT);
                    }


//                case R.id.renameThis:
//                    showChangeLangDialog(projectName, listItemPosition);

                    return true;
                default:
            }
            return false;
        }
    }

//    public void showChangeLangDialog(final String projectName, final int listItemPosition) {
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
//        final View dialogView = inflater.inflate(R.layout.dialog_rename, null);
//        dialogBuilder.setView(dialogView);
//
//        final EditText edt = (EditText) dialogView.findViewById(R.id.rename);
//
//        dialogBuilder.setTitle("Rename the project?");
//        dialogBuilder.setMessage("Enter the new project name:");
//        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//
//                File oldName = new File(Environment.getExternalStorageDirectory(),Master.getMyProjectsPath()+"/"+projectName);
//                File newName = new File(Environment.getExternalStorageDirectory(),Master.getMyProjectsPath()+"/"+edt.getText().toString());
//                boolean success = oldName.renameTo(newName);
//                ProjectsShowcase renamingStub = mAlbumList.get(listItemPosition);
//                mAlbumList.remove(listItemPosition);
//                renamingStub.setName(edt.getText().toString());
//                mAlbumList.add(renamingStub);
//                Log.d("Renaming stub working","---------Yea");
//                notifyDataSetChanged();
//            }
//        });
//        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int whichButton) {
//                //Just close the dialog
//            }
//        });
//        AlertDialog b = dialogBuilder.create();
//        b.show();
//    }


    @Override
    public int getItemCount() {
        return mProjectViewsList.size();
    }
}
