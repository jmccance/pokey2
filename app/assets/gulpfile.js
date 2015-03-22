var gulp = require('gulp');
var babel = require('gulp-babel');
var bower = require('gulp-bower');
var del = require('del');
var rename = require('gulp-regex-rename');

// Write output to the Play public directory.
var target = '../../public/dist'
var src = {
  scripts: 'es/**/*.es'
}

var target = {
  scripts: target + '/js',
  lib: target + '/lib'
}

gulp.task('clean', function(cb) {
  del([
   target + '/*'
  ],
  { force: true },
  cb);
});

gulp.task('bower', function () {
  return bower()
    .pipe(gulp.dest(target.lib));
})

gulp.task('compile', function () {
  return gulp.src(src.scripts)
    .pipe(babel())
    .pipe(rename(/\.es$/, '.js'))
    .pipe(gulp.dest(target.scripts));
});

gulp.task('default', ['bower', 'clean', 'compile']);

gulp.task('watch', function() {
  gulp.watch(src.scripts, ['default']);
});
